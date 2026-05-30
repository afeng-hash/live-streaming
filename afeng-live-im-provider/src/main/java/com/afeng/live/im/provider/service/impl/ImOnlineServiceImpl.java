package com.afeng.live.im.provider.service.impl;


import com.afeng.live.im.constants.ImCoreServerConstants;
import com.afeng.live.im.provider.service.ImOnlineService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 判断用户是否在线接口实现类
 */
@Service
public class ImOnlineServiceImpl implements ImOnlineService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断用户是否在线
     * @param userId
     * @param appId
     * @return
     */
    @Override
    public boolean isOnline(Long userId, Integer appId) {
        return stringRedisTemplate.hasKey(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":"+userId);
    }
}
