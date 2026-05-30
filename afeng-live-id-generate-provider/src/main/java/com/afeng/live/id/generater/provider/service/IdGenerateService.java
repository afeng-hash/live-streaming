package com.afeng.live.id.generater.provider.service;

public interface IdGenerateService {
    /**
     * 根据本地步长度来生成唯一 id(区间性递增)
     *
     * @return
     */
    Long getSeqId(Long id) throws Exception;
    /**
     * 生成的是非连续性 id
     *
     * @param id
     * @return
     */
    Long getUnSeqId(Long id) throws Exception;
    /**
     * 根据本地步长度来生成唯一 id(区间性递增)
     *
     * @param id
     * @return
     */
    String increaseSeqStrId(Integer id);
}
