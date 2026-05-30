package com.afeng.live.gift.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShopCarRespDto {

    private Long userId;
    private Integer roomId;
    private List<ShopCarItemRespDto> shopCarItemRespDtoList;
}
