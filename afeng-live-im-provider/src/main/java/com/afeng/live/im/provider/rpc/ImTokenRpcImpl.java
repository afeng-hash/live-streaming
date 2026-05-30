package com.afeng.live.im.provider.rpc;

import com.afeng.live.im.interfaces.ImTokenRpc;
import com.afeng.live.im.provider.service.ImTokenService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户登录rpc
 */
@DubboService
public class ImTokenRpcImpl implements ImTokenRpc {
    @Resource
    private ImTokenService imTokenService;

    @Override
    public String createImLoginToken(Long userId,Integer appId) {
        return imTokenService.createImLoginToken(userId,appId);
    }

    @Override
    public Long getUserIdByToken(String token) {
        return imTokenService.getUserIdByToken( token);
    }
}
