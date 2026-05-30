package com.afeng.live.id.generater.provider.service.bo;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地序列
 */
@Data
public class LocalSeqIdBO {

    private Long id;

    //在内存中记录的当前有序id的值
    private AtomicLong currentNum;

    //当前id段的开始值
    private Long currentStart;

    //当前id段的阈值
    private Long nextThreadshold;

}
