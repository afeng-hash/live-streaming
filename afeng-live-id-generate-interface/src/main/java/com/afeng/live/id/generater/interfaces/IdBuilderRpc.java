package com.afeng.live.id.generater.interfaces;

public interface IdBuilderRpc {
    /**
     * 根据本地步长度来生成唯一 id(区间性递增)
     *
     * @return
     */
    Long increaseSeqId(Long code);
    /**
     * 生成的是非连续性 id
     *
     * @param code
     * @return
     */
    Long increaseUnSeqId(Long code);
    /**
     * 根据本地步长度来生成唯一 id(区间性递增)
     *
     * @param code
     * @return
     */
    String increaseSeqStrId(int code);
}
