package com.afeng.live.sms.interfaces.rpc;


import com.afeng.live.sms.interfaces.enums.MsgSendResultEnum;
import com.afeng.live.sms.interfaces.msg.MsgCheckDTO;

public interface ISmsRpc {

    /**
     * 发送短信登录验证码接口
     *
     * @param phone
     * @return
     */
    MsgSendResultEnum sendLoginCode(String phone);

    /**
     * 校验登录验证码
     *
     * @param phone
     * @param code
     * @return
     */
    MsgCheckDTO checkLoginCode(String phone, Integer code);

}
