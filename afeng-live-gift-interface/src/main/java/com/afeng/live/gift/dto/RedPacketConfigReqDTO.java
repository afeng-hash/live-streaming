package com.afeng.live.gift.dto;


import lombok.Data;

import java.util.Date;

@Data
public class RedPacketConfigReqDTO {
    private Integer id;

    /**
     * 主播id
     */
    private Long anchordId;

    /**
     * 用户id
     */
    private Long userId;

    private Integer status;

    /**
     * 总金额
     */
    private Integer totalPrice;
    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 备注
     */
    private String remark;
    /**
     * 直播间id
     */
    private Integer roomId;

    private String redPacketConfigCode;

}
