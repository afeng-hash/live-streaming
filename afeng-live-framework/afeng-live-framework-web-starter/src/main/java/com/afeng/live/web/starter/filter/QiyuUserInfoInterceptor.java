package com.afeng.live.web.starter.filter;

import com.afeng.live.common.interfaces.enums.GatewayHeaderEnum;
import com.afeng.live.web.starter.constants.RequestConstants;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


public class AfengUserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIdStr = request.getHeader(GatewayHeaderEnum.USER_LOGIN_ID.getName());
        //参数判断，userID是否为空
        //可能走的是白名单url
        if (StringUtils.isEmpty(userIdStr)) {
            return true;
        }
        //如果userId不为空，则把它放在线程本地变量里面去
        AfengRequestContext.set(RequestConstants.AFENG_USER_ID, Long.valueOf(userIdStr));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        AfengRequestContext.clear();
    }
}
