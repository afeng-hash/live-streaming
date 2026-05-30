package com.afeng.live.gift.provider.service;

import com.afeng.live.gift.provider.dao.po.SkuInfoPO;

import java.util.List;

public interface ISkuInfoService {

    /**
     * 批量查询sku信息
     * @param skuIdList
     * @return
     */
    List<SkuInfoPO> queryBySkuIds(List<Long> skuIdList);

    /**
     * 根据skuId查询sku信息
     * @param skuId
     * @return
     */
    SkuInfoPO queryBySkuId(Long skuId);

    /**
     * 根据skuId从缓存中查询sku信息
     * @param skuId
     * @return
     */
    SkuInfoPO querySkuIdFromCache(Long skuId);
}
