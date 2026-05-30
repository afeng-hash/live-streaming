package com.afeng.live.user.provider.rpc;

import com.afeng.live.user.constants.UserTagsEnum;
import com.afeng.live.user.interfaces.IUserTagRpc;
import com.afeng.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户标签RPC
 */
@DubboService
public class UserTagRpcImpl implements IUserTagRpc {
    @Resource
    private IUserTagService iUserTagService;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        return iUserTagService.setTag(userId,userTagsEnum);
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return iUserTagService.cancelTag(userId,userTagsEnum);
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        return iUserTagService.containTag(userId,userTagsEnum);
    }
}
