package com.afeng.live.gift.provider.rpc;

import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.interfaces.ISkuStockInfoRpc;
import com.afeng.live.gift.provider.dao.po.SkuStockInfoPO;
import com.afeng.live.gift.provider.service.IAnchorShopInfoService;
import com.afeng.live.gift.provider.service.ISkuStockInfoService;
import com.afeng.live.gift.provider.service.bo.DcrStockNumBO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@DubboService
public class SkuStockInfoRpcImpl implements ISkuStockInfoRpc {
    @Resource
    private ISkuStockInfoService iSkuStockInfoService;
    @Resource
    private IAnchorShopInfoService anchorShopInfoService;
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder giftProviderCacheKeyBuilder;

    private final int MAX_TRY_TIMES = 5;

    /**
     * 根据skuId更新库存值
     * @param skuId
     * @return
     */
    @Override
    public Boolean dcrStockNumBySkuId(Long skuId, Integer num) {
        for (int i = 0; i < MAX_TRY_TIMES; i++) {
            DcrStockNumBO dcrStockNumBO = iSkuStockInfoService.dcrStockNumBySkuId(skuId, num);
            if (dcrStockNumBO.isNoStock()){
                return false;
            } else if (dcrStockNumBO.isSuccess()){
                return true;
            }
        }
        return false;
    }

    /**
     * 从库存值mysqkl预热加载到redis中
     * @param anchorId
     * @return
     */
    @Override
    public Boolean prepareStockInfo(Long anchorId) {
        List<Long> skuIdList = anchorShopInfoService.querySkuIdByAnchorId(anchorId);
        List<SkuStockInfoPO> skuStockInfoPOS = iSkuStockInfoService.queryBySkuIds(skuIdList);
        for (SkuStockInfoPO skuStockInfoPO : skuStockInfoPOS) {
            String key = giftProviderCacheKeyBuilder.buildSkuStockCache(skuStockInfoPO.getSkuId());
            redisTemplate.opsForValue().set(key,skuStockInfoPO.getStockNum(),1, TimeUnit.DAYS);
        }

        return true;
    }


    /**
     * 基础的缓存查询
     * @param skuId
     * @return
     */
    @Override
    public Integer queryStockNum(Long skuId) {
        String key = giftProviderCacheKeyBuilder.buildSkuStockCache(skuId);
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? 0 : (Integer) value;
    }

    /**
     * 同步库存数据到msyql
     * @param anchorId
     * @return
     */
    @Override
    public Boolean syncStockNumToMysql(Long anchorId) {
        List<Long> skuIdList = anchorShopInfoService.querySkuIdByAnchorId(anchorId);
        for (Long skuId : skuIdList) {
            Integer stockNum = this.queryStockNum(skuId);
            if (stockNum != 0){
                iSkuStockInfoService.updateStockNum(skuId,stockNum);
            }
        }
        return true;
    }

    /**
     * 根据skuId扣减库存值
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public Boolean decrStockBySkuIdV2(Long skuId, Integer num) {
        return iSkuStockInfoService.decrStockBySkuIdV2(skuId,num);
    }
}
