package com.afeng.live.msg.interfaces.dto;

import lombok.Data;

import java.beans.IntrospectionException;
import java.io.Serializable;
import java.util.Date;

@Data
public class MessageDto implements Serializable {
    /**
     * 发送人
     */
    private Long userId;

    /**
     * 接收人
     */
    private Long objectId;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 房间id
     */
    private Integer roomId;

    /**
     * 发送人名称
     */
    private String senderName;

    /**
     * 发送人头像
     */
    private String senderAvatar;

    private Date createTime;
    private Date updateTime;
}
