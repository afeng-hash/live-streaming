package com.afeng.live.gift.provider.config;

import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.interfaces.ISkuStockInfoRpc;
import com.afeng.live.gift.provider.service.IAnchorShopInfoService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class RefreshStockNumConfig implements InitializingBean {
    @Resource
    private ISkuStockInfoRpc iSkuStockInfoRpc;
    @Resource
    private IAnchorShopInfoService iAnchorShopInfoService;
    @Resource
    private GiftProviderCacheKeyBuilder giftProviderCacheKeyBuilder;
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    private ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(1);

    @Override
    public void afterPropertiesSet() throws Exception {
        //15刷新一次
        scheduledThreadPool.scheduleWithFixedDelay(new RefreshStockNumJob(),3000,15000, TimeUnit.MILLISECONDS);
    }

    class RefreshStockNumJob implements Runnable {

        @Override
        public void run() {
            String key = giftProviderCacheKeyBuilder.buildSkuStockLock();
            Boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(key, 1, 14, TimeUnit.SECONDS);
            if (lockStatus){
                List<Long> anchordIdList = iAnchorShopInfoService.queryAllValidAnchorId();
                for (Long anchordId : anchordIdList) {
                    iSkuStockInfoRpc.syncStockNumToMysql(anchordId);
                }
            }
        }
    }
}
