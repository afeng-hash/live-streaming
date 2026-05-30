package com.afeng.live.im.interfaces;

/**
 * 判断用户是否在线rpc
 */
public interface ImOnlineRpc {

    /**
     * 判断用户是否在线
     * @param userId
     * @param appId
     * @return
     */
    boolean isOnline(Long userId,Integer appId);
}
