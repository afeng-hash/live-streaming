package com.afeng.live.gift.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuDetailInfoDto implements Serializable {
    private Long skuId;
    private Integer skuPrice;
    private String skuCode;
    private String name;
    private String iconUrl; //缩略图
    private String originalIconUrl; //原图
    private Integer status;
    private String remark;
}
