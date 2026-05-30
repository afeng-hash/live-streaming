package com.afeng.live.gift.provider.service.impl;

import com.afeng.live.bank.interfaces.IAfengCurrencyAccountRpc;
import com.afeng.live.common.interfaces.common.GiftProviderTopicNames;
import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.common.interfaces.utils.ListUtils;
import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.bo.SendRedPacketBO;
import com.afeng.live.gift.dto.RedPacketConfigReqDTO;
import com.afeng.live.gift.dto.RedPacketReceiveDTO;
import com.afeng.live.gift.provider.dao.mapper.RedPacketConfigMapper;
import com.afeng.live.gift.provider.dao.po.RedPacketConfigPO;
import com.afeng.live.gift.provider.service.IRedPacketConfigService;
import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.router.interfaces.constants.ImMsgBizCodeEnum;
import com.afeng.live.im.router.interfaces.rpc.ImRouterRpc;
import com.afeng.living.interfaces.dto.LivingRoomReqDTO;
import com.afeng.living.interfaces.dto.LivingRoomRespDTO;
import com.afeng.living.interfaces.rpc.ILivingRoomRpc;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.lettuce.core.RedisException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedPacketConfigServiceImpl implements IRedPacketConfigService {

    @Resource
    private RedPacketConfigMapper redPacketConfigMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder giftProviderCacheKeyBuilder;
    @Resource
    private ImRouterRpc imRouterRpc;
    @Resource
    private ILivingRoomRpc iLivingRoomRpc;
    @Resource
    private IAfengCurrencyAccountRpc iAfengCurrencyAccountRpc;
    @Resource
    private MQProducer mqProducer;

    /**
     * 根据主播id查询红包雨配置
     * @param anchordId
     * @return
     */
    @Override
    public RedPacketConfigPO queryByAnchorId(Long anchordId) {
        LambdaQueryWrapper<RedPacketConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedPacketConfigPO::getAnchordId,anchordId);
        queryWrapper.eq(RedPacketConfigPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.orderByDesc(RedPacketConfigPO::getCreateTime);
        queryWrapper.last("limit 1");
        return redPacketConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean addOne(RedPacketConfigPO redPacketConfigPO) {
        redPacketConfigPO.setConfigCode(UUID.randomUUID().toString());
        return redPacketConfigMapper.insert(redPacketConfigPO) > 0;
    }

    @Override
    public boolean updateById(RedPacketConfigPO redPacketConfigPO) {
        return redPacketConfigMapper.updateById(redPacketConfigPO) > 0;
    }


    /**
     * 准备红包
     * @param anchordId
     * @return
     */
    @Override
    public boolean prepareRedPacket(Long anchordId) {
        //防止重复生成，以及错误参数传递情况
        RedPacketConfigPO redPacketConfigPO = this.queryByAnchorId(anchordId);
        if (redPacketConfigPO != null){
            return false;
        }
        //加锁控制
        boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(giftProviderCacheKeyBuilder.buildRedPacketInitLock(""),1,3, TimeUnit.SECONDS);
        if (!lockStatus){
            return false;
        }
        Integer totalCount = redPacketConfigPO.getTotalCount();
        Integer totalPrice = redPacketConfigPO.getTotalPrice();
        List<Integer> priceList = this.createRedPacketPriceList(totalPrice,totalCount);
        String code = redPacketConfigPO.getConfigCode();
        String key = giftProviderCacheKeyBuilder.buildRedPacketListCacheKey(code);
        //redis 输入输出缓冲区
        List<List<Integer>> splistList = ListUtils.splistList(priceList, 100);
        for (List<Integer> integerList : splistList) {
            redisTemplate.opsForList().leftPushAll(key,integerList);
        }
        redisTemplate.expire(key,1,TimeUnit.DAYS);
        //更新状态
        redPacketConfigPO.setStatus(CommonStatusEum.INVALID_STATUS.getCode());
        this.updateById(redPacketConfigPO);
        redisTemplate.opsForValue().set(giftProviderCacheKeyBuilder.buildRedPacketPrepareSuccessCache(code),1,1, TimeUnit.DAYS);
        return true;
    }


    /**
     * 领取红包
     * @param redPacketConfigReqDTO
     * @return
     */
    @Override
    public RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO redPacketConfigReqDTO) {
        String code = redPacketConfigReqDTO.getRedPacketConfigCode();
        String cacheKey = giftProviderCacheKeyBuilder.buildRedPacketListCacheKey(code);
        Object priceObj = redisTemplate.opsForList().rightPop(cacheKey);
        if(priceObj == null){
            return null;
        }

        // todo lua脚本去记录最大值
        log.info("[receiveRedPacket] code is {},priceObj is {}",code,priceObj);

        SendRedPacketBO sendRedPacketBO = new SendRedPacketBO();
        sendRedPacketBO.setPrice((Integer) priceObj);
        sendRedPacketBO.setReqDTO(redPacketConfigReqDTO);
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.RECEIVE_RED_PACKET);
        message.setBody(JSON.toJSONBytes(sendRedPacketBO));
        try {
            SendResult sendResult = mqProducer.send(message);
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())){
                return new RedPacketReceiveDTO((Integer) priceObj,true);
            }
        }catch (Exception e){
            throw new RedisException(e);
        }
        return new RedPacketReceiveDTO((Integer) priceObj,false);
    }


    /**
     * 开始抢红波，通知直播间人
     * @param reqDTO
     * @return
     */
    @Override
    public Boolean startRedPacket(RedPacketConfigReqDTO reqDTO) {
        String code = reqDTO.getRedPacketConfigCode();
        if (!redisTemplate.hasKey(giftProviderCacheKeyBuilder.buildRedPacketPrepareSuccessCache(code))) {
            return false;
        }
        String notifySuccessCache = giftProviderCacheKeyBuilder.buildRedPacketNotifyCache(code);
        if (!redisTemplate.hasKey(notifySuccessCache)) {
            //判断是否进行了抢红包动作，防止重复通知
            return false;
        }
        RedPacketConfigPO configPO = this.queryByConfigCode(code);
        //im广播事件
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("redPacketConfig", JSON.toJSONString(configPO));
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(reqDTO.getRoomId());
        livingRoomReqDTO.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
        List<Long> userIds = iLivingRoomRpc.queryUserIdsByRoomId(livingRoomReqDTO);
        if (CollectionUtils.isEmpty(userIds)){
            return false;
        }
        batchSendImMsg(userIds,ImMsgBizCodeEnum.START_RED_PACKET,jsonObject);
        redisTemplate.opsForValue().set(notifySuccessCache,1,1, TimeUnit.DAYS);
        return true;
    }

    /**
     * 根据配置码查询红包配置
     * @param configCode
     * @return
     */
    @Override
    public RedPacketConfigPO queryByConfigCode(String configCode) {
        LambdaQueryWrapper<RedPacketConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedPacketConfigPO::getConfigCode,configCode);
        queryWrapper.eq(RedPacketConfigPO::getStatus,CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.orderByDesc(RedPacketConfigPO::getCreateTime);
        queryWrapper.last("limit 1");
        return redPacketConfigMapper.selectOne(queryWrapper);
    }

    /**
     * 处理红包领取
     * @param reqDTO
     * @param price
     */
    @Override
    public void receiveRedPacketHandle(RedPacketConfigReqDTO reqDTO, Integer price) {
        String code = reqDTO.getRedPacketConfigCode();
        String totalGetCache = giftProviderCacheKeyBuilder.buildRedPacketTotalGetCache(code);
        String redPacketTotalGetPriceCache = giftProviderCacheKeyBuilder.buildRedPacketTotalGetPriceCache(code);
        redisTemplate.opsForValue().increment(giftProviderCacheKeyBuilder.buildUserTotalGetPriceCache(reqDTO.getUserId()),price);
        redisTemplate.opsForValue().increment(totalGetCache);
        redisTemplate.expire(totalGetCache, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().increment(redPacketTotalGetPriceCache,price);
        redisTemplate.expire(redPacketTotalGetPriceCache, 1, TimeUnit.DAYS);
        iAfengCurrencyAccountRpc.incr(reqDTO.getUserId(),price);
        redPacketConfigMapper.incrTotalGetPrice(code,price);
        redPacketConfigMapper.incrTotalGet(code);
    }


    /**
     * 批量发送im消息
     *
     * @param userIdList
     * @param imMsgBizCodeEnum
     * @param jsonObject
     */
    private void batchSendImMsg(List<Long> userIdList, ImMsgBizCodeEnum imMsgBizCodeEnum, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(imMsgBizCodeEnum.getCode());
            imMsgBody.setUserId(userId);
            imMsgBody.setData(jsonObject.toJSONString());
            return imMsgBody;
        }).collect(Collectors.toList());
        imRouterRpc.batchSendMsg(imMsgBodies);
    }


    /**
     * 生成红包金额List集合数据
     * @param totalPrice
     * @param totalCount
     * @return
     */
    private List<Integer> createRedPacketPriceList(Integer totalPrice, Integer totalCount) {
        List<Integer> redPacketPriceList = new ArrayList<>(totalCount);
        for (Integer i = 0; i < totalCount; i++) {
            if (i == totalCount - 1){
                redPacketPriceList.add(totalPrice);
                break;
            }
            int maxLimit = (totalPrice / (totalCount-i)) *2;
            int currentPrice = ThreadLocalRandom.current().nextInt(1,maxLimit);
            redPacketPriceList.add(currentPrice);
            totalPrice -= currentPrice;
        }
        return redPacketPriceList;
    }
}
