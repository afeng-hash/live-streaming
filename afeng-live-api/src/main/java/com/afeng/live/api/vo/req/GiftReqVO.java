package com.afeng.live.api.vo.req;

import lombok.Data;

/**
 * 礼物请求参数
 *
 * @Author: idea
 * @Date: Created in 10:05 2023/8/6
 * @Description
 */
@Data
public class GiftReqVO {
    private int giftId;
    private Integer roomId;
    //发送人
    private Long senderUserId;
    //接收人
    private Long receiverId;
    //直播类型
    private int type;
}
