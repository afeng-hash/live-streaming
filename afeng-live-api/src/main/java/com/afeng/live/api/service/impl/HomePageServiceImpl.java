package com.afeng.live.api.service.impl;

import com.afeng.live.api.service.IHomePageService;
import com.afeng.live.api.vo.HomePageVO;
import com.afeng.live.user.constants.UserTagsEnum;
import com.afeng.live.user.dto.UserDto;
import com.afeng.live.user.interfaces.IUserRpc;
import com.afeng.live.user.interfaces.IUserTagRpc;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.stereotype.Service;

/**
 * @Author idea
 * @Date: Created in 23:03 2023/7/19
 * @Description
 */
@Service
public class HomePageServiceImpl implements IHomePageService {

    @DubboReference
    private IUserRpc userRpc;
    @DubboReference
    private IUserTagRpc userTagRpc;

    @Override
    public HomePageVO initPage(Long userId) {
        UserDto userDTO = userRpc.getUserById(userId);
        HomePageVO homePageVO = new HomePageVO();
        if (userDTO != null) {
            homePageVO.setAvatar(userDTO.getAvatar());
            homePageVO.setUserId(userId);
            homePageVO.setNickName(userDTO.getNickName());
            //vip用户有权利开播
            homePageVO.setShowStartLivingBtn(userTagRpc.containTag(userId, UserTagsEnum.IS_VIP));
        }
        return homePageVO;
    }
}
