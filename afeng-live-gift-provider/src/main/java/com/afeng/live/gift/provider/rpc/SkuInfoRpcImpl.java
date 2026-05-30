package com.afeng.live.gift.provider.rpc;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.dto.SkuDetailInfoDto;
import com.afeng.live.gift.dto.SkuInfoDto;
import com.afeng.live.gift.interfaces.ISkuInfoRpc;
import com.afeng.live.gift.provider.dao.po.SkuInfoPO;
import com.afeng.live.gift.provider.service.IAnchorShopInfoService;
import com.afeng.live.gift.provider.service.ISkuInfoService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@DubboService
public class SkuInfoRpcImpl implements ISkuInfoRpc {

    @Resource
    private ISkuInfoService iSkuInfoService;
    @Resource
    private IAnchorShopInfoService iAnchorShopInfoService;



    /**
     * 根据主播id查询sku信息
     * @param anchorId
     * @return
     */
    @Override
    public List<SkuInfoDto> queryByAnchorId(Long anchorId) {
        List<Long> skuIdList = iAnchorShopInfoService.querySkuIdByAnchorId(anchorId);
        if (skuIdList == null || skuIdList.isEmpty()){
            return new ArrayList<>();
        }
        List<SkuInfoPO> skuInfoPOS = iSkuInfoService.queryBySkuIds(skuIdList);
        return ConvertBeanUtils.convertList(skuInfoPOS, SkuInfoDto.class);
    }

    /**
     * 查询商品详情
     * @param skuId
     * @return
     */
    @Override
    public SkuDetailInfoDto queryBySkuId(Long skuId) {
        return ConvertBeanUtils.convert(iSkuInfoService.querySkuIdFromCache(skuId), SkuDetailInfoDto.class);
    }
}
