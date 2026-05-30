package com.afeng.live.gift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class ShopCarItemRespDto implements Serializable {

    private Integer count;
    private SkuInfoDto skuInfoDto;
}
