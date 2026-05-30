package com.afeng.live.api.service.impl;

import com.afeng.live.account.interfaces.IAccountTokenRPC;
import com.afeng.live.api.error.ApiErrorEnum;
import com.afeng.live.api.service.IUserLoginService;
import com.afeng.live.api.vo.UserLoginVO;
import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.common.interfaces.vo.WebResponseVO;

import com.afeng.live.sms.interfaces.enums.MsgSendResultEnum;
import com.afeng.live.sms.interfaces.msg.MsgCheckDTO;
import com.afeng.live.sms.interfaces.rpc.ISmsRpc;
import com.afeng.live.user.dto.UserLoginDTO;
import com.afeng.live.user.interfaces.IUserPhoneRPC;
import com.afeng.live.user.interfaces.IUserRpc;
import com.afeng.live.web.starter.error.ErrorAssert;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserLoginServiceImpl implements IUserLoginService {
    private static String PHONE_REG = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginServiceImpl.class);

    @DubboReference
    private ISmsRpc smsRpc;
    @DubboReference
    private IUserPhoneRPC userPhoneRPC;
    @DubboReference
    private IUserRpc userRpc;
    @DubboReference
    private IAccountTokenRPC accountTokenRPC;

    @Override
    public WebResponseVO sendLoginCode(String phone) {
        ErrorAssert.isNotBlank(phone, ApiErrorEnum.PHONE_IS_EMPTY);
        ErrorAssert.isTure(Pattern.matches(PHONE_REG, phone), ApiErrorEnum.PHONE_IN_VALID);
        MsgSendResultEnum msgSendResultEnum = smsRpc.sendLoginCode(phone);
        if (msgSendResultEnum == MsgSendResultEnum.SEND_SUCCESS) {
            return WebResponseVO.success();
        }
        return WebResponseVO.sysError("短信发送太频繁，请稍后再试");
    }

    @Override
    public WebResponseVO login(String phone, Integer code, HttpServletResponse response) {
        ErrorAssert.isNotBlank(phone, ApiErrorEnum.PHONE_IS_EMPTY);
        ErrorAssert.isTure(Pattern.matches(PHONE_REG, phone), ApiErrorEnum.PHONE_IN_VALID);
        ErrorAssert.isTure(code != null && code > 1000, ApiErrorEnum.SMS_CODE_ERROR);
        MsgCheckDTO msgCheckDTO = smsRpc.checkLoginCode(phone, code);
        if (!msgCheckDTO.isCheckStatus()) {
            return WebResponseVO.bizError(msgCheckDTO.getDesc());
        }
        //验证码校验通过
        UserLoginDTO userLoginDTO = userPhoneRPC.login(phone);
        ErrorAssert.isTure(userLoginDTO.isLoginSuccess(),ApiErrorEnum.USER_LOGIN_ERROR);

        String token = accountTokenRPC.createAndSaveLoginToken(userLoginDTO.getUserId());

//        String token = userLoginDTO.getDesc();
        Cookie cookie = new Cookie("qytk", token);
        //http://app.afeng.live.com/html/afeng_live_list_room.html
        //http://api.afeng.live.com/live/api/userLogin/sendLoginCode
//        cookie.setDomain("afeng.live.com");
        cookie.setPath("/");
        //cookie有效期，一般他的默认单位是秒
        cookie.setMaxAge(30 * 24 * 3600);
        //加上它，不然web浏览器不会将cookie自动记录下
        response.addCookie(cookie);
        return WebResponseVO.success(ConvertBeanUtils.convert(userLoginDTO, UserLoginVO.class));
    }
}
