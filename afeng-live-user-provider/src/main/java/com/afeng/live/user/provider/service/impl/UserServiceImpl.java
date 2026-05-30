package com.afeng.live.user.provider.service.impl;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.framework.redis.starter.keys.UserProviderCacheKeyBuilder;
import com.afeng.live.user.constants.CacheAsyncDeleteCode;
import com.afeng.live.user.constants.UserProviderTopicNames;
import com.afeng.live.user.dto.UserCacheAsyncDeleteDto;
import com.afeng.live.user.dto.UserDto;
import com.afeng.live.user.provider.dao.mapper.UserMapper;
import com.afeng.live.user.provider.dao.po.UserPO;
import com.afeng.live.user.provider.service.IUserService;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author afeng
 * @date 2023/5/14 20:05
 **/
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;
    @Autowired
    private MQProducer mqProducer;


    @Override
    public UserDto getUserById(Long userId) {
        if (userId == null){
            return null;
        }

        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userId);
        UserDto userDto = (UserDto)redisTemplate.opsForValue().get(key);
        if(userDto != null){
            System.out.println("从缓存中获取用户信息");
            return userDto;
        }

        userDto = ConvertBeanUtils.convert(userMapper.selectById(userId), UserDto.class);
        if (userDto != null){
            redisTemplate.opsForValue().set(key, userDto,30, TimeUnit.MINUTES);
        }

        return userDto;
    }

    /**
     * 更新用户信息
     * @param userDto
     * @return
     */
    @Override
    public boolean updateUserInfo(UserDto userDto) {
        if (userDto == null || userDto.getUserId() == null){
            return false;
        }

        int i = userMapper.updateById(ConvertBeanUtils.convert(userDto, UserPO.class));
        //缓存删除
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userDto.getUserId());
        redisTemplate.delete( key);

        try {
            //封装消息提
            UserCacheAsyncDeleteDto userCacheAsyncDeleteDto = new UserCacheAsyncDeleteDto();
            userCacheAsyncDeleteDto.setCode(CacheAsyncDeleteCode.USER_INFO_DELETE.getCode());
            Map<String,Object> jsonMap = new HashMap<>();
            jsonMap.put("userId",userDto.getUserId());
            userCacheAsyncDeleteDto.setJson(JSON.toJSONString(jsonMap));

            Message message = new Message();
            message.setTopic(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC);
            message.setDelayTimeLevel(1);  //延迟1秒
            message.setBody(JSON.toJSONString(userCacheAsyncDeleteDto).getBytes());
            mqProducer.send( message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return i > 0;
    }



    @Override
    public boolean insertOne(UserDto userDto) {
        if (userDto == null){
            return false;
        }
        int i = userMapper.insert(ConvertBeanUtils.convert(userDto, UserPO.class));
        return i > 0;
    }


    /**
     * 批量查询用户信息
     *
     * @param userIdList
     * @return
     */
    @Override
    public Map<Long, UserDto> batchQueryUserByIds(List<Long> userIdList) {
        if(CollectionUtils.isEmpty(userIdList)){
            return Collections.emptyMap();
        }
        userIdList = userIdList.stream().filter(userId -> userId > 10000).toList();
        if (CollectionUtils.isEmpty(userIdList)){
            return Collections.emptyMap();
        }

        //redis
        List<String> keyList = new ArrayList<>();
        userIdList.forEach(userId -> keyList.add(userProviderCacheKeyBuilder.buildUserInfoKey(userId)));
        List<UserDto> userDtoList = redisTemplate.opsForValue().multiGet(keyList).stream().filter(Objects::nonNull).map(obj -> (UserDto)obj).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(userDtoList) && userDtoList.size() == userIdList.size()){
            return userDtoList.stream().collect(Collectors.toMap(UserDto::getUserId, userDto -> userDto));
        }

        List<Long> userIdInCacheList = userDtoList.stream().map(UserDto::getUserId).toList();
        List<Long> userIdNotInCacheList = userIdList.stream().filter(userId -> !userIdInCacheList.contains(userId)).toList();

        //这个方法底层会查询多张表然后union all进行合并，这样性能不好
//        userMapper.selectBatchIds(userIdList);
        //我们可以采用多线程并在本地内存聚合的方式去做分表的数据查询
        //将不同的id分为不同的list，交给不同的线程去查询
        Map<Long, List<Long>> listMap = userIdNotInCacheList.stream().collect(Collectors.groupingBy(id -> id % 100));
        List<UserDto> dbQueryResult = new CopyOnWriteArrayList<>();
        listMap.values().parallelStream().forEach(list -> {
            List<UserDto> userDtos = userMapper.selectBatchIds(list).stream().filter(Objects::nonNull).map(po -> ConvertBeanUtils.convert(po, UserDto.class)).toList();
            if (!CollectionUtils.isEmpty(userDtos)){
                dbQueryResult.addAll(userDtos);
            }
        });

        //对redis进行一个补偿
        if(!CollectionUtils.isEmpty(dbQueryResult)){
            Map<String,UserDto> saveCacheMap = dbQueryResult.stream().collect(Collectors.toMap(userDto -> userProviderCacheKeyBuilder.buildUserInfoKey(userDto.getUserId()), userDto -> userDto));
            redisTemplate.opsForValue().multiSet(saveCacheMap);
            //使用管道符传输命令，减小网络IO
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public @Nullable <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (String key : saveCacheMap.keySet()) {
                        operations.expire((K) key,createRandomExpireTime(),TimeUnit.MINUTES);
                    }
                    return null;
                }
            });

            userDtoList.addAll(dbQueryResult);
        }

        return userDtoList.stream().collect(Collectors.toMap(UserDto::getUserId, userDto -> userDto));
    }

    //随机过期时间
    private int createRandomExpireTime(){
        return new Random().nextInt(30) + 30;
    }
}
