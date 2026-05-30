package com.afeng.live.im.core.server.interfaces.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * im用户登录发送mq信息实体类
 */
@Data
public class ImOnlineDto implements Serializable {
    private Long userId;
    private Integer appId;
    private Integer roomId;
    private Long loginTime;
}
