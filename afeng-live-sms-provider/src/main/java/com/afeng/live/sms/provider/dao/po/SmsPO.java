package com.afeng.live.sms.provider.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@TableName("t_sms")
@Data
public class SmsPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer code;
    private String phone;
    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private Date sendTime;
    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private Date updateTime;

}
