package com.afeng.live.gift.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_sku_order_info")
public class SkuOrderInfoPO {
    @TableId(type= IdType.AUTO)
    private Long id;
    private String skuIdList;
    private Long userId;
    private Integer roomId;
    private String extra;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
