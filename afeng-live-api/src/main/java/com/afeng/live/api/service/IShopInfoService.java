package com.afeng.live.api.service;

import com.afeng.live.api.vo.req.ShopCarReqVO;
import com.afeng.live.api.vo.req.SkuInfoReqVO;
import com.afeng.live.api.vo.resp.ShopCarRespVO;
import com.afeng.live.api.vo.resp.SkuDetailInfoVO;
import com.afeng.live.api.vo.resp.SkuInfoVo;
import com.afeng.live.gift.dto.ShopCarReqDto;
import com.afeng.live.gift.dto.ShopCarRespDto;

import java.util.List;

public interface IShopInfoService {

    /**
     * 根据房间id查询商品信息
     * @param roomId
     * @return
     */
    List<SkuInfoVo> queryByRoomId(Integer roomId);

    /**
     * 查询商品详情
     * @param skuInfoReqVO
     * @return
     */
    SkuDetailInfoVO detail(SkuInfoReqVO skuInfoReqVO);

    /**
     * 添加购物车商品
     * @return
     */
    Boolean addCar(ShopCarReqVO shopCarReqVO);


    /**
     * 展示购物车信息
     * @param shopCarReqVO
     * @return
     */
    ShopCarRespVO getCarInfo(ShopCarReqVO shopCarReqVO);


    /**
     * 移除购物车商品
     * @param shopCarReqVO
     * @return
     */
    Boolean removeFromCar(ShopCarReqVO shopCarReqVO);

    /**
     * 清空购物车
     * @param shopCarReqVO
     * @return
     */
    Boolean clearShopCar(ShopCarReqVO shopCarReqVO);

    /**
     * 修改商品数量
     * @param shopCarReqVO
     * @return
     */
    Boolean addCarItemNum(ShopCarReqVO shopCarReqVO);
}
