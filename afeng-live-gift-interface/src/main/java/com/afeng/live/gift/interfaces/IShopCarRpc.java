package com.afeng.live.gift.interfaces;

import com.afeng.live.gift.dto.ShopCarReqDto;
import com.afeng.live.gift.dto.ShopCarRespDto;

public interface IShopCarRpc {
    /**
     * 添加购物车商品
     * @param shopCarReqDto
     * @return
     */
    Boolean addCar(ShopCarReqDto shopCarReqDto);

    /**
     * 展示购物车信息
     * @param shopCarReqDto
     * @return
     */
    ShopCarRespDto getCarInfo(ShopCarReqDto shopCarReqDto);


    /**
     * 移除购物车商品
     * @param shopCarReqDto
     * @return
     */
    Boolean removeFromCar(ShopCarReqDto shopCarReqDto);

    /**
     * 清空购物车
     * @param shopCarReqDto
     * @return
     */
    Boolean clearShopCar(ShopCarReqDto shopCarReqDto);

    /**
     * 修改商品数量
     * @param shopCarReqDto
     * @return
     */
    Boolean addCarItemNum(ShopCarReqDto shopCarReqDto);
}
