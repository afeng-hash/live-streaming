package com.afeng.live.web.starter.limit;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLimit {

    /**
     * 请求限制次数
     * @return
     */
    int limit();

    /**
     * 请求限制时间
     * @return
     */
    int second();

    /**
     * 提示信息
     * @return
     */
    String msg() default "请求过于频繁";
}
