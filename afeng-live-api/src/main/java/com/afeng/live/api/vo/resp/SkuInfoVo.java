package com.afeng.live.api.vo.resp;

import lombok.Data;

@Data
public class SkuInfoVo {
    private Long skuId;
    private Integer skuPrice;
    private String skuCode;
    private String name;
    private String iconUrl; //缩略图
    private String originalIconUrl; //原图
    private Integer status;
    private String remark;
}
