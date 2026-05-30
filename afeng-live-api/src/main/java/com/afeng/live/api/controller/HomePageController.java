package com.afeng.live.api.controller;

import com.afeng.live.api.service.IHomePageService;
import com.afeng.live.api.vo.HomePageVO;
import com.afeng.live.common.interfaces.vo.WebResponseVO;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomePageController {

    @Resource
    private IHomePageService homePageService;

    @PostMapping("/initPage")
    public WebResponseVO initPage() {
        //前端调用这个，成功，表示token依旧有效
        Long userId = AfengRequestContext.getUserId();
        HomePageVO homePageVO = new HomePageVO();
        homePageVO.setLoginStatus(false);
        if (userId != null) {
            homePageVO = homePageService.initPage(userId);
            homePageVO.setLoginStatus(true);
        }
        return WebResponseVO.success(homePageVO);
    }
}
