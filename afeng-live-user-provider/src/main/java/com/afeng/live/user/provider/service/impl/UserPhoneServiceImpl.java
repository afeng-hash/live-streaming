package com.afeng.live.user.provider.service.impl;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.common.interfaces.utils.DESUtils;
import com.afeng.live.framework.redis.starter.keys.UserProviderCacheKeyBuilder;
import com.afeng.live.id.generater.enums.IdTypeEnum;
import com.afeng.live.id.generater.interfaces.IdBuilderRpc;
import com.afeng.live.user.dto.UserDto;
import com.afeng.live.user.dto.UserLoginDTO;
import com.afeng.live.user.dto.UserPhoneDTO;
import com.afeng.live.user.provider.dao.mapper.IUserPhoneMapper;
import com.afeng.live.user.provider.dao.po.UserPhonePO;
import com.afeng.live.user.provider.service.IUserPhoneService;
import com.afeng.live.user.provider.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserPhoneServiceImpl implements IUserPhoneService {
    @Autowired
    private IUserPhoneMapper userPhoneMapper;
    @Autowired
    private UserProviderCacheKeyBuilder cacheKeyBuilder;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private IUserService userService;
    @DubboReference
    private IdBuilderRpc idBuilderRpc;

    /**
     * 登录
     * @param phone
     * @return
     */
    @Override
    public UserLoginDTO login(String phone) {
        if (StringUtils.isEmpty(phone)){
            return null;
        }

        //是否注册过
        UserPhoneDTO userPhonePO = queryByPhone(phone);
        if (userPhonePO != null){
            log.info("[login] 用户已注册：{}",userPhonePO.getUserId());
            return UserLoginDTO.loginSuccess(userPhonePO.getUserId(),createLoginToken(userPhonePO.getUserId()));
        }

        //如果没有注册过，生成user信息，插入手机记录，绑定userid
        return register( phone);

    }


    /**
     * 注册
     * @param phone
     * @return
     */
    private UserLoginDTO register(String phone){
        Long userId = idBuilderRpc.increaseUnSeqId(IdTypeEnum.USER_ID.getCode());
        log.info("[register] 生成userId：{}",userId);
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        userDto.setNickName("旗鱼用户-"+userId);
        userService.insertOne(userDto);
        UserPhonePO userPhonePO = new UserPhonePO();
        userPhonePO.setUserId(userId);
        userPhonePO.setPhone(DESUtils.encrypt( phone));
        userPhonePO.setStatus(CommonStatusEum.VALID_STATUS.getCode());
        userPhoneMapper.insert(userPhonePO);
        redisTemplate.delete(cacheKeyBuilder.buildUserPhoneObjKey(phone));
        return UserLoginDTO.loginSuccess(userPhonePO.getUserId(),createLoginToken(userPhonePO.getUserId()));
    }


    /**
     * 创建登录token
     * @param userId
     * @return
     */
    public String createLoginToken(Long userId){
        String token = UUID.randomUUID().toString();
        String key = cacheKeyBuilder.buildUserLoginTokenKey(token);
        redisTemplate.opsForValue().set(key,userId,30, TimeUnit.MINUTES);
        return token;
    }


    @Override
    public UserPhoneDTO queryByPhone(String phone) {
        if (StringUtils.isEmpty(phone)){
            return null;
        }

        String redisKey = cacheKeyBuilder.buildUserPhoneObjKey(phone);
        UserPhoneDTO userPhoneDTO = (UserPhoneDTO) redisTemplate.opsForValue().get(redisKey);
        if (userPhoneDTO != null ){
            if (userPhoneDTO.getUserId() == null){
                return null;
            }
            return userPhoneDTO;
        }
        userPhoneDTO = queryByPhoneFromDB(phone);
        if (userPhoneDTO != null){
            redisTemplate.opsForValue().set(redisKey,userPhoneDTO,30,TimeUnit.MINUTES);
            return userPhoneDTO;
        }
        //缓存击穿，空对象
        userPhoneDTO = new UserPhoneDTO();
        redisTemplate.opsForValue().set(redisKey,userPhoneDTO,5,TimeUnit.MINUTES);
        return null;
    }


    /**
     * 根据userId查询phone
     * @param userId
     * @return
     */
    @Override
    public List<UserPhoneDTO> queryByUserId(Long userId) {
        if (userId == null){
            return Collections.emptyList();
        }

        String key = cacheKeyBuilder.buildUserPhoneListKey(userId);
        List<Object> userPhoneList = redisTemplate.opsForList().range(key, 0, -1);
        if (!CollectionUtils.isEmpty(userPhoneList)){
            if (((UserPhoneDTO)userPhoneList.get(0)).getUserId() == null){
                return Collections.emptyList();
            }
            return userPhoneList.stream().map(x-> (UserPhoneDTO)x).collect(Collectors.toList());
        }

        List<UserPhoneDTO> userPhoneDTOS = this.queryByUserIdFromDB(userId);
        if (!CollectionUtils.isEmpty(userPhoneDTOS)){
            redisTemplate.opsForList().leftPushAll(key,userPhoneDTOS.toArray());
            redisTemplate.expire(key,30,TimeUnit.MINUTES);
            return userPhoneDTOS;
        }
        //缓存击穿
        redisTemplate.opsForList().leftPushAll(key,new UserPhoneDTO());
        redisTemplate.expire(key,5,TimeUnit.MINUTES);

        return Collections.emptyList();
    }

    /**
     * 从数据库根据userId查询phone
     * @param userId
     * @return
     */
    private List<UserPhoneDTO> queryByUserIdFromDB(Long userId) {
        LambdaQueryWrapper<UserPhonePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhonePO::getUserId, userId);
        queryWrapper.eq(UserPhonePO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        List<UserPhonePO> userPhonePOS = userPhoneMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(userPhonePOS)){
            return Collections.emptyList();
        }
        return ConvertBeanUtils.convertList(userPhonePOS, UserPhoneDTO.class);
    }


    /**
     * 从数据库查询
     * @param phone
     * @return
     */
    private UserPhoneDTO queryByPhoneFromDB(String phone){
        LambdaQueryWrapper<UserPhonePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhonePO::getPhone, DESUtils.encrypt(phone));
        queryWrapper.eq(UserPhonePO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convert(userPhoneMapper.selectOne(queryWrapper),UserPhoneDTO.class);
    }


}
