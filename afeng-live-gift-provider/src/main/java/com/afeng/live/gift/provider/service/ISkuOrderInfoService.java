package com.afeng.live.gift.provider.service;

import com.afeng.live.gift.dto.SkuOrderInfoDto;

import java.util.List;

public interface ISkuOrderInfoService {
    /**
     * 支持多直播间内用户下单的订单查询
     * @param userId
     * @param roomId
     * @return
     */
    SkuOrderInfoDto queryByUserIdAndRoomId(Long userId, Long roomId);

    /**
     * 插入一条订单信息
     * @param skuOrderInfoDto
     * @return
     */
    boolean insertOne(SkuOrderInfoDto skuOrderInfoDto);

    /**
     * 根据订单di修改状态
     * @param orderId
     * @param status
     * @return
     */
    boolean updateOrderStatus(Long orderId,Integer status);
}
