package com.afeng.live.user.provider.service.impl;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.framework.redis.starter.keys.UserProviderCacheKeyBuilder;
import com.afeng.live.user.constants.CacheAsyncDeleteCode;
import com.afeng.live.user.constants.UserProviderTopicNames;
import com.afeng.live.user.constants.UserTagFieldNameConstants;
import com.afeng.live.user.constants.UserTagsEnum;
import com.afeng.live.user.dto.UserCacheAsyncDeleteDto;
import com.afeng.live.user.dto.UserTagDto;
import com.afeng.live.user.provider.dao.mapper.UserTagMapper;
import com.afeng.live.user.provider.dao.po.UserTagPO;
import com.afeng.live.user.provider.service.IUserTagService;
import com.afeng.live.user.provider.utis.TagInfoUtils;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户标签服务实现类
 */
@Service
public class IUserTagServiceImpl implements IUserTagService {

    @Autowired
    private UserTagMapper userTagMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private UserProviderCacheKeyBuilder cacheKeyBuilder;
    @Autowired
    private MQProducer mqProducer;

    /**
     * 设置标签
     * @param userId
     * @param userTagsEnum
     * @return
     */
    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO == null){
            userTagPO = new UserTagPO();
            userTagPO.setUserId(userId);
            if (userTagsEnum.getFieldName().equals(UserTagFieldNameConstants.FIELD_NAME_TAG_INFO_01) ){
                userTagPO.setTagInfo01(userTagsEnum.getTag());
            }else if (userTagsEnum.getFieldName() .equals(UserTagFieldNameConstants.FIELD_NAME_TAG_INFO_02) ){
                userTagPO.setTagInfo02(userTagsEnum.getTag());
            }else if (userTagsEnum.getFieldName().equals(UserTagFieldNameConstants.FIELD_NAME_TAG_INFO_03) ){
                userTagPO.setTagInfo03(userTagsEnum.getTag());
            }
            userTagMapper.insert(userTagPO);
            return true;
        }

        boolean updateStatus = userTagMapper.setTag(userId, userTagsEnum.getTag(), userTagsEnum.getFieldName()) > 0;
        if (updateStatus){
            deleteUserTagDtoFromRedis(userId);
            return true;
        }

        return userTagMapper.setTag(userId,userTagsEnum.getTag(),userTagsEnum.getFieldName()) > 0;
    }

    /**
     * 取消标签
     * @param userId
     * @param userTagsEnum
     * @return
     */
    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean flag = userTagMapper.cancelTag(userId, userTagsEnum.getTag(), userTagsEnum.getFieldName()) > 0;
        if (!flag){
            return false;
        }
        deleteUserTagDtoFromRedis(userId);
        return true;
    }

    /**
     * 是否包含某个标签
     * @param userId
     * @param userTagsEnum
     * @return
     */
    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagDto userTagDto = queryByUserIdFromRedis(userId);
        if (userTagDto == null){
            return false;
        }

        String queryFieldName = userTagsEnum.getFieldName();
        if (UserTagFieldNameConstants.FIELD_NAME_TAG_INFO_01.equals(queryFieldName)){
            return TagInfoUtils.isContain(userTagDto.getTagInfo01(),userTagsEnum.getTag());
        }else if (UserTagFieldNameConstants.FIELD_NAME_TAG_INFO_02.equals(queryFieldName)){
            return TagInfoUtils.isContain(userTagDto.getTagInfo02(),userTagsEnum.getTag());
        }else if (UserTagFieldNameConstants.FIELD_NAME_TAG_INFO_03.equals(queryFieldName)){
            return TagInfoUtils.isContain(userTagDto.getTagInfo03(),userTagsEnum.getTag());
        }

        return false;
    }


    /**
     * 删除缓存中用户标签
     * @param userId
     */
    private void deleteUserTagDtoFromRedis(Long userId){
        String key = cacheKeyBuilder.buildUserTagKey(userId);
        redisTemplate.delete(key);

        UserCacheAsyncDeleteDto userCacheAsyncDeleteDto = new UserCacheAsyncDeleteDto();
        userCacheAsyncDeleteDto.setCode(CacheAsyncDeleteCode.USER_TAG_DELETE.getCode());
        Map<String,Object> jsonMap = new HashMap<>();
        jsonMap.put("userId",userId);
        userCacheAsyncDeleteDto.setJson(JSON.toJSONString(jsonMap));
        try {
            Message message = new Message();
            message.setTopic(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC);
            message.setDelayTimeLevel(1);  //延迟1秒
            message.setBody(JSON.toJSONString(userCacheAsyncDeleteDto).getBytes());
            mqProducer.send( message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 从缓存中查询用户标签
     * @param userId
     * @return
     */
    private UserTagDto queryByUserIdFromRedis(Long userId){
        String key = cacheKeyBuilder.buildUserTagKey(userId);
        UserTagDto userTagDto = (UserTagDto) redisTemplate.opsForValue().get(key);
        if (userTagDto != null){
            return userTagDto;
        }

        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO == null){
            return null;
        }
        userTagDto = ConvertBeanUtils.convert(userTagPO, UserTagDto.class);
        redisTemplate.opsForValue().set(key,userTagDto,30, TimeUnit.MINUTES);
        return userTagDto;
    }
}
