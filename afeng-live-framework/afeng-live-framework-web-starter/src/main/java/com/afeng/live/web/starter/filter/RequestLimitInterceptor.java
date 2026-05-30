package com.afeng.live.web.starter.filter;

import com.afeng.live.web.starter.error.AfengErrorException;
import com.afeng.live.web.starter.limit.RequestLimit;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 对于重复请求，要有专门的拦截器去处理
 *
 * @Author idea
 * @Date: Created in 14:06 2023/8/5
 * @Description
 */
public class RequestLimitInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLimitInterceptor.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            boolean hasLimit = handlerMethod.getMethod().isAnnotationPresent(RequestLimit.class);
            if (hasLimit) {
                //是否需要限制请求
                RequestLimit requestLimit = handlerMethod.getMethod().getAnnotation(RequestLimit.class);
                Long userId = AfengRequestContext.getUserId();
                if (userId == null) {
                    //对于没有登录的用户
                    //设置一个默认的userId
                    userId = 0L;
//                    return true;
                }
                //(userId + requestValue),md5,->string,
                // /user/login
                String requestKey = applicationName + ":" + request.getRequestURI() + ":" + userId;
                int limit = requestLimit.limit();
                int second = requestLimit.second();

                //利用increment的原子操作，增加并返回
                Long currentCount = redisTemplate.opsForValue().increment(requestKey, 1);

                //如果是首次请求
                if (currentCount != null && currentCount == 1) {
                    redisTemplate.expire(requestKey, second, TimeUnit.SECONDS);
                }
                //超出限制
                if (currentCount != null && currentCount > limit) {
                    //直接抛出全局异常，让异常捕获器处理
                    LOGGER.error("[RequestLimitInterceptor] userId is {},req too much", userId);
                    throw new AfengErrorException(-1, requestLimit.msg());
                }
            } else {
                return true;
            }
        }
        return true;
    }
}

