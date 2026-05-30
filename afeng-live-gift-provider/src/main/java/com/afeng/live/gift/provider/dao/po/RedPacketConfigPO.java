package com.afeng.live.gift.provider.dao.po;

import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_red_packet_config")
public class RedPacketConfigPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 主播id
     */
    private Long anchordId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 一共领取的红包数
     */
    private Integer totalGet;
    /**
     * 一共领取的红包金额
     */
    private Integer totalGetPrice;
    /**
     * 最大领取金额
     */
    private Integer maxGetPrice;
    /**
     * 状态
     * @see CommonStatusEum
     */
    private Integer status;
    /**
     * 总金额
     */
    private Integer totalPrice;
    /**
     * 总数量
     */
    private Integer totalCount;
    /**
     * 配置编号
     */
    private String configCode;
    /**
     * 备注
     */
    private String remark;
    private Date createTime;
    private Date updateTime;
}
