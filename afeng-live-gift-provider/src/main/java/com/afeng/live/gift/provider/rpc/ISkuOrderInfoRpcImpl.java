package com.afeng.live.gift.provider.rpc;

import com.afeng.live.gift.dto.SkuOrderInfoDto;
import com.afeng.live.gift.interfaces.ISkuOrderInfoRpc;
import com.afeng.live.gift.provider.service.ISkuOrderInfoService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class ISkuOrderInfoRpcImpl implements ISkuOrderInfoRpc {

    @Resource
    private ISkuOrderInfoService iSkuOrderInfoService;

    @Override
    public SkuOrderInfoDto queryByUserIdAndRoomId(Long userId, Long roomId) {
        return iSkuOrderInfoService.queryByUserIdAndRoomId(userId,roomId);
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
