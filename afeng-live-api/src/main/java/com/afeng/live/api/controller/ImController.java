package com.afeng.live.api.controller;

import com.afeng.live.api.service.ImService;
import com.afeng.live.common.interfaces.vo.WebResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/im")
public class ImController {

    @Resource
    private ImService imService;

    /**
     * 获取im配置
     *
     * @return
     */
    @PostMapping("/getImConfig")
    public WebResponseVO getImConfig() {
        return WebResponseVO.success(imService.getImConfig());
    }
}
