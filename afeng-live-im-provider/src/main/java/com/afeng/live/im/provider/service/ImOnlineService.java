package com.afeng.live.im.provider.service;

/**
 * 用户是否在线接口
 */
public interface ImOnlineService {

    /**
     * 判断用户是否在线
     * @param userId
     * @param appId
     * @return
     */
    boolean isOnline(Long userId,Integer appId);
}
