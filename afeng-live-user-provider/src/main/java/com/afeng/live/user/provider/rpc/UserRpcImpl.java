package com.afeng.live.user.provider.rpc;

import com.afeng.live.user.dto.UserDto;
import com.afeng.live.user.interfaces.IUserRpc;
import com.afeng.live.user.provider.service.IUserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class UserRpcImpl implements IUserRpc {

    @Resource
    private IUserService userService;

    @Override
    public UserDto getUserById(Long userId) {
        return userService.getUserById(userId);
    }

    @Override
    public boolean updateUserInfo(UserDto userDto) {
        return userService.updateUserInfo(userDto);
    }

    @Override
    public boolean insertOne(UserDto userDto) {
        return userService.insertOne(userDto);
    }

    @Override
    public Map<Long, UserDto> batchQueryUserByIds(List<Long> userIdList) {
        return userService.batchQueryUserByIds(userIdList);
    }
}
