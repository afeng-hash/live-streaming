package com.afeng.live.bank.api.service.impl;

import com.afeng.live.bank.api.service.IPayNotifyService;
import com.afeng.live.bank.api.vo.WxPayNotifyVO;
import com.afeng.live.bank.dto.PayOrderDTO;
import com.afeng.live.bank.interfaces.IPayOrderRpc;
import com.alibaba.fastjson2.JSON;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.stereotype.Service;

/**
 * @Author idea
 * @Date: Created in 21:50 2023/8/19
 * @Description
 */
@Service
public class PayNotifyServiceImpl implements IPayNotifyService {

    @DubboReference
    private IPayOrderRpc payOrderRpc;

    @Override
    public String notifyHandler(String paramJson) {
        WxPayNotifyVO wxPayNotifyVO = JSON.parseObject(paramJson, WxPayNotifyVO.class);
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setUserId(wxPayNotifyVO.getUserId());
        payOrderDTO.setBizCode(wxPayNotifyVO.getBizCode());
        payOrderDTO.setOrderId(wxPayNotifyVO.getOrderId());
        return payOrderRpc.payNotify(payOrderDTO) ? "success" : "fail";
    }
}
