package com.afeng.live.gift.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * sku库存表
 */
@TableName("t_sku_stock_info")
@Data
public class SkuStockInfoPO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Long skuId;
    private Integer stockNum;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer version;
}
