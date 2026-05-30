package com.afeng.live.api.controller;

import com.afeng.live.api.service.IShopInfoService;
import com.afeng.live.api.vo.req.ShopCarReqVO;
import com.afeng.live.api.vo.req.SkuInfoReqVO;
import com.afeng.live.common.interfaces.vo.WebResponseVO;
import com.afeng.live.gift.dto.ShopCarReqDto;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop")
public class ShopInfoController {

    @Resource
    private IShopInfoService iShopInfoService;

    /**
     * 查询商品列表
     *
     * @param roomId
     * @return
     */
    @PostMapping("/listSkuInfo")
    public WebResponseVO listSkuInfo(Integer roomId) {
        return WebResponseVO.success(iShopInfoService.queryByRoomId(roomId));
    }

    /**
     * 商品详情
     *
     * @param skuInfoReqVO
     * @return
     */
    @PostMapping("/detail")
    public WebResponseVO detail(SkuInfoReqVO skuInfoReqVO) {
        return WebResponseVO.success(iShopInfoService.detail(skuInfoReqVO));
    }

    /**
     * 往购物车添加商品
     * @return
     */
    @PostMapping("/addCar")
    public WebResponseVO addCar(ShopCarReqVO reqVO) {
        return WebResponseVO.success(iShopInfoService.addCar(reqVO));
    }

    /**
     * 从购物车删除商品
     * @return
     */
    @PostMapping("/removeFromCar")
    public WebResponseVO removeFromCar(ShopCarReqVO reqVO) {
        return WebResponseVO.success(iShopInfoService.removeFromCar(reqVO));
    }

    /**
     * 获取购物车信息
     * @return
     */
    @PostMapping("/getCarInfo")
    public WebResponseVO getCarInfo(ShopCarReqVO shopCarReqVO) {
        return WebResponseVO.success(iShopInfoService.getCarInfo(shopCarReqVO));
    }

    /**
     * 清空购物车
     * @return
     */
    @PostMapping("/clearCar")
    public WebResponseVO clearCar(ShopCarReqVO shopCarReqVO) {
        return WebResponseVO.success(iShopInfoService.clearShopCar(shopCarReqVO));
    }
}
