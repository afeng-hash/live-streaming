package com.afeng.live.sms.provider.rpc;


import com.afeng.live.sms.interfaces.enums.MsgSendResultEnum;
import com.afeng.live.sms.interfaces.msg.MsgCheckDTO;
import com.afeng.live.sms.interfaces.rpc.ISmsRpc;
import com.afeng.live.sms.provider.service.ISmsService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class SmsRpcImpl implements ISmsRpc {
    @Resource
    private ISmsService smsService;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        return smsService.sendLoginCode(phone);
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        return smsService.checkLoginCode(phone,code);
    }

}
