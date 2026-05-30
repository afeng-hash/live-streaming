package com.afeng.live.gift.interfaces;

public interface ISkuStockInfoRpc {
    /**
     * 根据skuId更新库存值
     * @param skuId
     * @return
     */
    Boolean dcrStockNumBySkuId(Long skuId,Integer num);

    //从库存值mysqkl预热加载到redis中
    Boolean prepareStockInfo(Long anchorId);

    /**
     * 基础的缓存查询
     * @param skuId
     * @return
     */
    Integer queryStockNum(Long skuId);

    /**
     * 同步库存数据到msyql
     * @param anchorId
     * @return
     */
    Boolean syncStockNumToMysql(Long anchorId);

    /**
     * 根据skuId扣减库存值
     * @param skuId
     * @param num
     * @return
     */
    Boolean decrStockBySkuIdV2(Long skuId,Integer num);
}
