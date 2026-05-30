package com.afeng.live.gift.provider.service.impl;

import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.provider.dao.mapper.SkuStockInfoMapper;
import com.afeng.live.gift.provider.dao.po.SkuStockInfoPO;
import com.afeng.live.gift.provider.service.ISkuStockInfoService;
import com.afeng.live.gift.provider.service.bo.DcrStockNumBO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SkuStockInfoServiceImpl implements ISkuStockInfoService {
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder giftProviderCacheKeyBuilder;
    @Resource
    private SkuStockInfoMapper skuStockInfoMapper;

    private String LUA_SCRIPT =
            "if (redis.call('exists',KEYS[1])) == 1 then" +
                    "local currentStock = redis.call('get',KEYS[1])" +
                    "if (tonumber(currentStock)>0 and tonumber(currentStock) - tonumber(ARGV[1]) >= 0) then"+
                    "return redis.call('decrby',KEYS[1],tonumber(ARGV[1]))" +
                    "else return -1 end"+
                    "else" +
                    "return -1 end";


    /**
     * 更新库存
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public Boolean updateStockNum(Long skuId, Integer num) {
        SkuStockInfoPO skuStockInfoPO = new SkuStockInfoPO();
        skuStockInfoPO.setStockNum(num);
        LambdaUpdateWrapper<SkuStockInfoPO> qw = new LambdaUpdateWrapper<>();
        qw.eq(SkuStockInfoPO::getSkuId,skuId);
        skuStockInfoMapper.update(skuStockInfoPO,qw);
        return true;
    }

    @Override
    public DcrStockNumBO dcrStockNumBySkuId(Long skuId, Integer num) {
        SkuStockInfoPO skuStockInfoPO = this.queryBySkuId(skuId);
        DcrStockNumBO dcrStockNumBO = new DcrStockNumBO();
        if (skuStockInfoPO.getStockNum()==0 || skuStockInfoPO.getStockNum() - num < 0){
            dcrStockNumBO.setNoStock(true);
            dcrStockNumBO.setSuccess(false);
        }
        boolean updateState = skuStockInfoMapper.descStockNumBySkuId(skuId,num,skuStockInfoPO.getVersion())>0;
        dcrStockNumBO.setSuccess(updateState);
        dcrStockNumBO.setNoStock(false);
        return dcrStockNumBO;
    }

    @Override
    public SkuStockInfoPO queryBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuStockInfoPO> qw = new LambdaQueryWrapper<>();
        qw.eq(SkuStockInfoPO::getSkuId,skuId);
        qw.eq(SkuStockInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        qw.last("limit 1");
        return skuStockInfoMapper.selectOne(qw);
    }

    /**
     * 批量查询库存信息
     * @param skuIdList
     * @return
     */
    @Override
    public List<SkuStockInfoPO> queryBySkuIds(List<Long> skuIdList) {
        LambdaQueryWrapper<SkuStockInfoPO> qw = new LambdaQueryWrapper<>();
        qw.in(SkuStockInfoPO::getSkuId,skuIdList);
        qw.eq(SkuStockInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        return skuStockInfoMapper.selectList(qw);
    }

    /**
     * 根据skuId扣减库存值
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public Boolean decrStockBySkuIdV2(Long skuId, Integer num) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(LUA_SCRIPT);
        redisScript.setResultType(Long.class);
        String key = giftProviderCacheKeyBuilder.buildSkuStockCache(skuId);

        return redisTemplate.execute(redisScript, Collections.singletonList(key),num) >= 0;
    }
}
