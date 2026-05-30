package com.afeng.live.gift.provider.service.impl;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.common.interfaces.common.GiftProviderTopicNames;
import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.dto.GiftConfigDto;
import com.afeng.live.gift.provider.dao.mapper.GiftConfigMapper;
import com.afeng.live.gift.provider.dao.po.GiftConfigPO;
import com.afeng.live.gift.provider.service.IGiftConfigService;
import com.afeng.live.gift.provider.service.bo.GiftCacheRemoveBO;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class IGiftServiceImpl implements IGiftConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IGiftServiceImpl.class);

    @Resource
    private GiftConfigMapper giftConfigMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    private MQProducer mqProducer;

    /**
     * 根据礼物id查询礼物信息
     *
     * @param giftId
     * @return
     */
    @Override
    public GiftConfigDto getByGifgId(Integer giftId) {
        String cacheKey = cacheKeyBuilder.buildGiftConfigCacheKey(giftId);
        //使用缓存去抵挡对db层的访问压力
        GiftConfigDto GiftConfigDto = (GiftConfigDto) redisTemplate.opsForValue().get(cacheKey);
        if (GiftConfigDto != null) {
            if (GiftConfigDto.getGiftId() != null) {
                redisTemplate.expire(cacheKey, 60, TimeUnit.MINUTES);
                return GiftConfigDto;
            }
            //空值缓存
            return null;
        }
        //借助redis的string
        LambdaQueryWrapper<GiftConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GiftConfigPO::getGiftId, giftId);
        queryWrapper.eq(GiftConfigPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        GiftConfigPO giftConfigPO = giftConfigMapper.selectOne(queryWrapper);
        //检索出来的数据，要重新存入cache中
        if (giftConfigPO != null) {
            GiftConfigDto configDTO = ConvertBeanUtils.convert(giftConfigPO, GiftConfigDto.class);
            //如果存在该对象，则缓存到redis中
            redisTemplate.opsForValue().set(cacheKey, configDTO, 60, TimeUnit.MINUTES);
            return configDTO;
        }
        //避免二次请求对db的访问压力
        //假设说 我们是一个非常大的并发场景，大量的请求落入到getByGiftId方法中，假设我们的后台下架了某个礼物
        redisTemplate.opsForValue().set(cacheKey, new GiftConfigDto(), 5, TimeUnit.MINUTES);
        return null;
    }

    @Override
    public List<GiftConfigDto> queryGiftList() {
        String cacheKey = cacheKeyBuilder.buildGiftListCacheKey();
        //礼物的列表数据不会特别多，直接进行list的全量便利
        List<GiftConfigDto> cacheList = redisTemplate.opsForList().range(cacheKey, 0, 100).stream()
                .map(x -> (GiftConfigDto) x).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(cacheList)) {
            //不是空list缓存
            if (cacheList.get(0).getGiftId() != null) {
                redisTemplate.expire(cacheKey, 60, TimeUnit.MINUTES);
                return cacheList;
            }
            return Collections.emptyList();
        }
        //如果为空：一种是空值缓存（放了一个空的list集合），另一种是缓存过期了
        //list集合去进行存放
        LambdaQueryWrapper<GiftConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GiftConfigPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        List<GiftConfigPO> giftConfigPOList = giftConfigMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(giftConfigPOList)) {
            List<GiftConfigDto> resultList = ConvertBeanUtils.convertList(giftConfigPOList, GiftConfigDto.class);
            boolean trySetToRedis = redisTemplate.opsForValue().setIfAbsent(cacheKeyBuilder.buildGiftListLockCacheKey(),1,3,TimeUnit.SECONDS);
            if(trySetToRedis) {
                redisTemplate.opsForList().leftPushAll(cacheKey, resultList.toArray());
                //大部分情况下，一个直播间的有效时间大概就是60min以上
                redisTemplate.expire(cacheKey, 60, TimeUnit.MINUTES);
            }
            return resultList;
        }
        //存入一个空的list进入redis中
        redisTemplate.opsForList().leftPush(cacheKey, new GiftConfigDto());
        redisTemplate.expire(cacheKey, 5, TimeUnit.MINUTES);
        return Collections.emptyList();
    }

    @Override
    public void insertOne(GiftConfigDto GiftConfigDto) {
        GiftConfigPO giftConfigPO = ConvertBeanUtils.convert(GiftConfigDto, GiftConfigPO.class);
        giftConfigPO.setStatus(CommonStatusEum.VALID_STATUS.getCode());
        giftConfigMapper.insert(giftConfigPO);
        redisTemplate.delete(cacheKeyBuilder.buildGiftListCacheKey());
        GiftCacheRemoveBO giftCacheRemoveBO = new GiftCacheRemoveBO();
        giftCacheRemoveBO.setRemoveListCache(true);
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.REMOVE_GIFT_CACHE);
        message.setBody(JSON.toJSONBytes(giftCacheRemoveBO));
        //1秒之后延迟消费
        message.setDelayTimeLevel(1);
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.info("[insertOne] sendResult is {}", sendResult);
        } catch (Exception e) {
            LOGGER.info("[insertOne] mq send error: }", e);
        }
    }

    @Override
    public void updateOne(GiftConfigDto GiftConfigDto) {
        GiftConfigPO giftConfigPO = ConvertBeanUtils.convert(GiftConfigDto, GiftConfigPO.class);
        giftConfigMapper.updateById(giftConfigPO);
        redisTemplate.delete(cacheKeyBuilder.buildGiftListCacheKey());
        redisTemplate.delete(cacheKeyBuilder.buildGiftConfigCacheKey(GiftConfigDto.getGiftId()));
        GiftCacheRemoveBO giftCacheRemoveBO = new GiftCacheRemoveBO();
        giftCacheRemoveBO.setRemoveListCache(true);
        giftCacheRemoveBO.setGiftId(GiftConfigDto.getGiftId());
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.REMOVE_GIFT_CACHE);
        message.setBody(JSON.toJSONBytes(giftCacheRemoveBO));
        //1秒之后延迟消费
        message.setDelayTimeLevel(1);
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.info("[updateOne] sendResult is {}", sendResult);
        } catch (Exception e) {
            LOGGER.info("[updateOne] mq send error: }", e);
        }
    }
}
