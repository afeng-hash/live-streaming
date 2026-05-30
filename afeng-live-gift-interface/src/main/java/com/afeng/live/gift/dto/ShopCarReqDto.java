package com.afeng.live.gift.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShopCarReqDto implements Serializable {
    private Long userId;
    private Long skuId;
    private Integer roomId;
}
