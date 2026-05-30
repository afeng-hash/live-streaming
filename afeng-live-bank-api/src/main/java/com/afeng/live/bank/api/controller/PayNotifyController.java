package com.afeng.live.bank.api.controller;

import com.afeng.live.bank.api.service.IPayNotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理支付回调的逻辑
 *
 * @Author idea
 * @Date: Created in 21:49 2023/8/19
 * @Description
 */
@Slf4j
@RestController
@RequestMapping("/payNotify")
public class PayNotifyController {

    @Resource
    private IPayNotifyService payNotifyService;

    /**
     * 微信回调
     *
     * @param param
     * @return
     */
    @PostMapping("/wxNotify")
    public String wxNotify(@RequestParam("param") String param) {
        log.info("[PayNotifyController] 收到微信的回调");
        return payNotifyService.notifyHandler(param);
    }

}
