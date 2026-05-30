package com.afeng.live.gift.provider.service.impl;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.dto.ShopCarItemRespDto;
import com.afeng.live.gift.dto.ShopCarReqDto;
import com.afeng.live.gift.dto.ShopCarRespDto;
import com.afeng.live.gift.dto.SkuInfoDto;
import com.afeng.live.gift.provider.dao.po.SkuInfoPO;
import com.afeng.live.gift.provider.service.IShopCarService;
import com.afeng.live.gift.provider.service.ISkuInfoService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShopCarServiceImpl implements IShopCarService {

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder giftProviderCacheKeyBuilder;
    @Autowired
    private ISkuInfoService iSkuInfoService;

    /**
     * 展示购物车数据
     * @param shopCarReqDto
     * @return
     */
    @Override
    public ShopCarRespDto getCarInfo(ShopCarReqDto shopCarReqDto) {
        String key = giftProviderCacheKeyBuilder.buildUserShopCarCache(shopCarReqDto.getUserId(), shopCarReqDto.getRoomId());
        Cursor<Map.Entry<Object, Object>> allCarData = redisTemplate.opsForHash().scan(key, ScanOptions.scanOptions().match("*").build());
        List<ShopCarItemRespDto> shopCarItemRespDtoList = new ArrayList<>();
        List<Long> skuIdLists = new ArrayList<>();
        Map<Long,Integer> skuCountMap = new HashMap<>();
        while (allCarData.hasNext()){
            Map.Entry<Object, Object> entry = allCarData.next();
            skuIdLists.add((Long) entry.getKey());
            skuCountMap.put((Long) entry.getKey(),(Integer) entry.getValue());
        }
        List<SkuInfoPO> skuInfoDtoList = iSkuInfoService.queryBySkuIds(skuIdLists);
        for (SkuInfoPO skuInfoPO : skuInfoDtoList) {
            SkuInfoDto skuInfoDto = ConvertBeanUtils.convert(skuInfoPO, SkuInfoDto.class);
            Integer count = skuCountMap.get(skuInfoDto.getSkuId());
            shopCarItemRespDtoList.add(new ShopCarItemRespDto(count,skuInfoDto));
        }
        ShopCarRespDto shopCarRespDto = new ShopCarRespDto();
        shopCarRespDto.setRoomId(shopCarReqDto.getRoomId());
        shopCarRespDto.setUserId(shopCarReqDto.getUserId());
        shopCarRespDto.setShopCarItemRespDtoList(shopCarItemRespDtoList);
        return shopCarRespDto;
    }

    /**
     * 添加商品到购物车中
     * @param shopCarReqDto
     * @return
     */
    @Override
    public Boolean addCar(ShopCarReqDto shopCarReqDto) {
        String key = giftProviderCacheKeyBuilder.buildUserShopCarCache(shopCarReqDto.getUserId(), shopCarReqDto.getRoomId());
        redisTemplate.opsForHash().put(key,shopCarReqDto.getSkuId(),1);
        return true;
    }

    @Override
    public Boolean removeFromCar(ShopCarReqDto shopCarReqDto) {
        String key = giftProviderCacheKeyBuilder.buildUserShopCarCache(shopCarReqDto.getUserId(), shopCarReqDto.getRoomId());
        redisTemplate.opsForHash().delete(key,shopCarReqDto.getSkuId());
        return true;
    }

    /**
     * 清空购物车
     * @param shopCarReqDto
     * @return
     */
    @Override
    public Boolean clearShopCar(ShopCarReqDto shopCarReqDto) {
        String key = giftProviderCacheKeyBuilder.buildUserShopCarCache(shopCarReqDto.getUserId(), shopCarReqDto.getRoomId());
        redisTemplate.delete(key);
        return true;
    }

    /**
     * 修改商品数量
     * @param shopCarReqDto
     * @return
     */
    @Override
    public Boolean addCarItemNum(ShopCarReqDto shopCarReqDto) {
        String key = giftProviderCacheKeyBuilder.buildUserShopCarCache(shopCarReqDto.getUserId(), shopCarReqDto.getRoomId());
        redisTemplate.opsForHash().increment(key,shopCarReqDto.getSkuId(),1);
        return true;
    }
}
