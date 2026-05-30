package com.afeng.live.gift.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 商品sku信息表
 */
@TableName("t_sku_info")
@Data
public class SkuInfoPO {
    @TableId(type= IdType.AUTO)
    private Long id;
    private Long skuId;
    private Integer skuPrice;
    private String skuCode;
    private String name;
    private String iconUrl; //缩略图
    private String originalIconUrl; //原图
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
