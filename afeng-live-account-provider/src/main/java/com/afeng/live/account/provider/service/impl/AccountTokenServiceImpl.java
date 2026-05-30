package com.afeng.live.account.provider.service.impl;

import com.afeng.live.account.provider.service.IAccountTokenService;
import com.afeng.live.framework.redis.starter.keys.AccountProviderCacheKeyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AccountTokenServiceImpl implements IAccountTokenService {
    @Autowired
    private AccountProviderCacheKeyBuilder accountProviderCacheKeyBuilder;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public String createAndSaveLoginToken(Long userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(accountProviderCacheKeyBuilder.buildUserLoginTokenKey(token),userId,30, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public Long getUserIdByToken(String tokenKey) {
        String redisKey = accountProviderCacheKeyBuilder.buildUserLoginTokenKey(tokenKey);
        Integer userId = (Integer) redisTemplate.opsForValue().get(redisKey);
        return userId == null ? null : Long.valueOf(userId);
    }

    /**
     * 续期
     * @param tokenKey
     */
    @Override
    public void expireToken(String tokenKey) {
        String redisKey = accountProviderCacheKeyBuilder.buildUserLoginTokenKey(tokenKey);
        redisTemplate.expire(redisKey,30,TimeUnit.MINUTES);
    }


}
