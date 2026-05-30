package com.afeng.live.user.provider.service;

import com.afeng.live.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface IUserService {

    UserDto getUserById(Long userId);


    boolean updateUserInfo(UserDto userDto);

    boolean insertOne(UserDto userDto);

    /**
     * 批量查询用户信息
     *
     * @param userIdList
     * @return
     */
    Map<Long, UserDto> batchQueryUserByIds(List<Long> userIdList);
}
