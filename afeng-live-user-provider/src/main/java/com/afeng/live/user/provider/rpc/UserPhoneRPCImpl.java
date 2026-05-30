package com.afeng.live.user.provider.rpc;

import com.afeng.live.user.dto.UserLoginDTO;
import com.afeng.live.user.dto.UserPhoneDTO;
import com.afeng.live.user.interfaces.IUserPhoneRPC;
import com.afeng.live.user.provider.service.IUserPhoneService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class UserPhoneRPCImpl implements IUserPhoneRPC {

    @Resource
    private IUserPhoneService userPhoneService;

    @Override
    public UserLoginDTO login(String phone) {
        return userPhoneService.login(phone);
    }

    @Override
    public UserPhoneDTO queryByPhone(String phone) {
        return userPhoneService.queryByPhone( phone);
    }

    @Override
    public List<UserPhoneDTO> queryByUserId(Long userId) {
        return userPhoneService.queryByUserId(userId);
    }
}
