package com.afeng.live.gift.interfaces;

import com.afeng.live.gift.dto.SkuDetailInfoDto;
import com.afeng.live.gift.dto.SkuInfoDto;

import java.util.List;

public interface ISkuInfoRpc {

    /**
     * 根据主播id查询sku信息
     * @param anchorId
     * @return
     */
    List<SkuInfoDto> queryByAnchorId(Long anchorId);


    /**
     * 根据skuId查询sku详情信息
     * @param skuId
     * @return
     */
    SkuDetailInfoDto queryBySkuId(Long skuId);
}
