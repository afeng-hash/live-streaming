package com.afeng.live.id.generater.provider.dao.po;

import lombok.Data;

import java.time.LocalDate;

@Data
public class IdGeneratePO {

    private Long id;
    private String remark;
    private Long nextThreshold;
    private Long initNum;
    private Long currentStart;
    private Long step;
    private Long isSeq;
    private String idPrefix;
    private Long version;
    private LocalDate createTime;
    private LocalDate updateTime;


}
