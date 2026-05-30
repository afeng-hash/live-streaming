package com.afeng.live.sms.interfaces.msg;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 发送消息的内容
 *
 * @Author idea
 * @Date: Created in 15:00 2023/7/11
 * @Description
 */
@Data
public class MessageDTO implements Serializable {

//    @Serial
//    private static final long serialVersionUID = -8982006120358366161L;
    private Long userId;
    private Integer roomId;
    //发送人名称
    private String senderName;
    //发送人头像
    private String senderAvtar;
    /**
     * 消息类型
     */
    private Integer type;
    /**
     * 消息内容
     */
    private String content;
    private Date createTime;
    private Date updateTime;
}
