package com.afeng.live.gift.provider.service.impl;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.gift.dto.SkuOrderInfoDto;
import com.afeng.live.gift.provider.dao.mapper.SkuOrderInfoMapper;
import com.afeng.live.gift.provider.dao.po.SkuOrderInfoPO;
import com.afeng.live.gift.provider.service.ISkuOrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuOrderInfoServiceImpl implements ISkuOrderInfoService {

    @Resource
    private SkuOrderInfoMapper skuOrderInfoMapper;

    @Override
    public SkuOrderInfoDto queryByUserIdAndRoomId(Long userId, Long roomId) {
        LambdaQueryWrapper<SkuOrderInfoPO> qw = new LambdaQueryWrapper<>();
        qw.eq(SkuOrderInfoPO::getUserId,userId);
        qw.eq(SkuOrderInfoPO::getRoomId,roomId);
        qw.orderByDesc(SkuOrderInfoPO::getId);
        qw.last("limit 1");
        SkuOrderInfoPO skuOrderInfoPO = skuOrderInfoMapper.selectOne(qw);
        if (skuOrderInfoPO != null){
            return ConvertBeanUtils.convert(skuOrderInfoPO, SkuOrderInfoDto.class);
        }
        return null;
    }

    @Override
    public boolean insertOne(SkuOrderInfoDto skuOrderInfoDto) {
        return false;
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        return false;
    }
}
