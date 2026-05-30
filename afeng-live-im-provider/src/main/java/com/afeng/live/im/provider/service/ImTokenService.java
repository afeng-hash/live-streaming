package com.afeng.live.im.provider.service;

public interface ImTokenService {
    /**
     * 创建im登录token
     * @param userId
     * @param appId
     * @return
     */
    String createImLoginToken(Long userId,Integer appId);

    /**
     * 验证im登录token
     * @param token
     * @return
     */
    Long getUserIdByToken(String token);
}
