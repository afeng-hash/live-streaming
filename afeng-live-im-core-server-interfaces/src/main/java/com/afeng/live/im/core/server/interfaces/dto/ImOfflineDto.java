package com.afeng.live.im.core.server.interfaces.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * im用户下线发送mq信息实体类
 */
@Data
public class ImOfflineDto implements Serializable {
    private Long userId;
    private Integer appId;
    private Integer roomId;
    private Long logoutTime;
}
