package com.afeng.live.im.provider.rpc;

import com.afeng.live.im.interfaces.ImOnlineRpc;
import com.afeng.live.im.provider.service.ImOnlineService;
import com.afeng.live.im.provider.service.impl.ImOnlineServiceImpl;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 判断用户是否在线rpc
 */
@DubboService
public class ImOnlineRpcImpl implements ImOnlineRpc {

    @Resource
    private ImOnlineService imOnlineService;

    /**
     * 判断用户是否在线
     * @param userId
     * @param appId
     * @return
     */
    @Override
    public boolean isOnline(Long userId, Integer appId) {
        return imOnlineService.isOnline(userId,appId);
    }
}
