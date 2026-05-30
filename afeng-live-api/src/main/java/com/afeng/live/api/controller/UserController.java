package com.afeng.live.api.controller;

import com.afeng.live.user.dto.UserDto;
import com.afeng.live.user.interfaces.IUserRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @DubboReference
    private IUserRpc userRpc;


    @GetMapping("/getUserInfo")
    public UserDto getUserInfo(Long userId) {
        return userRpc.getUserById(userId);
    }

    @GetMapping("/update")
    public boolean update(Long userId, String nickName) {
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        userDto.setNickName(nickName);
        return userRpc.updateUserInfo(userDto);
    }

    @GetMapping("/insertOne")
    public boolean insertOne(Long userId) {
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        userDto.setNickName("zhangsan");
        return userRpc.insertOne(userDto);
    }



    @GetMapping("/batchQuery")
    public Map<Long, UserDto> get(String userIdsStr) {
        return userRpc.batchQueryUserByIds(Arrays.asList(userIdsStr.split(",")).stream().map(Long::parseLong).collect(Collectors.toList()));
    }
}
