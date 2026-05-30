package com.afeng.live.gift.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RedPacketConfigRespDTO implements Serializable {
    private Integer id;
    private Integer totalPrice;
    private Integer totalCount;
    private String remark;
    private String configCode;
}
