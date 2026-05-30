package com.afeng.live.account.provider.rpc;

import com.afeng.live.account.interfaces.IAccountTokenRPC;
import com.afeng.live.account.provider.service.IAccountTokenService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class AccountTokenRPCImpl implements IAccountTokenRPC {
    @Resource
    private IAccountTokenService accountTokenService;

    @Override
    public String createAndSaveLoginToken(Long userId) {
        return accountTokenService.createAndSaveLoginToken(userId);
    }

    @Override
    public Long getUserIdByToken(String tokenKey) {
        return accountTokenService.getUserIdByToken(tokenKey);
    }

    @Override
    public void expireToken(String tokenKey) {
        accountTokenService.expireToken(tokenKey);
    }
}
