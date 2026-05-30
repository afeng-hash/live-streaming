package com.afeng.live.gift.provider.service.impl;

import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.provider.dao.mapper.SkuInfoMapper;
import com.afeng.live.gift.provider.dao.po.SkuInfoPO;
import com.afeng.live.gift.provider.service.ISkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SkuInfoServiceImpl implements ISkuInfoService {

    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder giftProviderCacheKeyBuilder;

    /**
     * 根据skuId列表查询sku信息
     * @param skuIdList
     * @return
     */
    @Override
    public List<SkuInfoPO> queryBySkuIds(List<Long> skuIdList) {
        LambdaQueryWrapper<SkuInfoPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SkuInfoPO::getId,skuIdList);
        lambdaQueryWrapper.eq(SkuInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        return skuInfoMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 根据skuId查询sku信息
     * @param skuId
     * @return
     */
    @Override
    public SkuInfoPO queryBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuInfoPO> qw = new LambdaQueryWrapper<>();
        qw.eq(SkuInfoPO::getId,skuId);
        qw.eq(SkuInfoPO::getStatus,CommonStatusEum.VALID_STATUS.getCode());
        qw.last("limit 1");
        return skuInfoMapper.selectOne(qw);
    }

    /**
     * 根据skuId查询sku信息从缓存中
     * @param skuId
     * @return
     */
    @Override
    public SkuInfoPO querySkuIdFromCache(Long skuId) {
        String key = giftProviderCacheKeyBuilder.buildSkuDetailCache(skuId);
        SkuInfoPO skuInfoPO = (SkuInfoPO)redisTemplate.opsForValue().get(key);
        if (skuInfoPO != null){
            return skuInfoPO;
        }
        skuInfoPO = this.queryBySkuId(skuId);
        if (skuInfoPO == null){
            return null;
        }
        redisTemplate.opsForValue().set(key,skuInfoPO,1, TimeUnit.DAYS);
        return skuInfoPO;
    }
}
