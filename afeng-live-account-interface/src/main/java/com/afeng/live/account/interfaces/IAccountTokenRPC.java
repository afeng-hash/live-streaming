package com.afeng.live.account.interfaces;

/**
 * 账户token服务
 */
public interface IAccountTokenRPC {


    /**
     * 创建一个登录token
     *
     * @param userId
     * @return
     */
    String createAndSaveLoginToken(Long userId);

    /**
     * 校验用户token
     *
     * @param tokenKey
     * @return
     */
    Long getUserIdByToken(String tokenKey);

    /**
     * 给token续期
     * @param tokenKey
     */
    void expireToken(String tokenKey);
}
