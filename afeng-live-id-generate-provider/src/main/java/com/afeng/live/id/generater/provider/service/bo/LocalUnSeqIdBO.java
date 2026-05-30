package com.afeng.live.id.generater.provider.service.bo;

import lombok.Data;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 本地无序id段
 */
@Data
public class LocalUnSeqIdBO {
    private Long id;

    //提前将无序的id存放如一个队列中
    private ConcurrentLinkedQueue<Long> idQueue;

    //当前id段的开始值
    private Long currentStart;

    //当前id段的阈值
    private Long nextThreadshold;
}
