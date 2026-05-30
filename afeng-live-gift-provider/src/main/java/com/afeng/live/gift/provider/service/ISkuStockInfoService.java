package com.afeng.live.gift.provider.service;

import com.afeng.live.gift.provider.dao.po.SkuStockInfoPO;
import com.afeng.live.gift.provider.service.bo.DcrStockNumBO;

import java.util.List;

public interface ISkuStockInfoService {

    /**
     * 更新库存
     * @param skuId
     * @param num
     * @return
     */
    Boolean updateStockNum(Long skuId,Integer num);

    /**
     * 根据skuId更新库存值
     * @param skuId
     * @return
     */
    DcrStockNumBO dcrStockNumBySkuId(Long skuId, Integer num);

    /**
     * 根据skuId查询库存信息
     * @param skuId
     * @return
     */
    SkuStockInfoPO queryBySkuId(Long skuId);

    /**
     * 批量查询库存信息
     * @param skuIdList
     * @return
     */
    List<SkuStockInfoPO> queryBySkuIds(List<Long> skuIdList);

    /**
     * 根据skuId扣减库存值
     * @param skuId
     * @param num
     * @return
     */
    Boolean decrStockBySkuIdV2(Long skuId,Integer num);
}
