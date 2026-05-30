package com.afeng.live.gift.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 带货主播权限配置表
 */
@TableName("t_anchor_shop_info")
@Data
public class AnchorShopInfoPO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Long anchorId;
    private Long skuId;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
