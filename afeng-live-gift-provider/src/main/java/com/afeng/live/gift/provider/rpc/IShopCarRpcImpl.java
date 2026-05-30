package com.afeng.live.gift.provider.rpc;

import com.afeng.live.gift.dto.ShopCarReqDto;
import com.afeng.live.gift.dto.ShopCarRespDto;
import com.afeng.live.gift.interfaces.IShopCarRpc;
import com.afeng.live.gift.provider.service.IShopCarService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class IShopCarRpcImpl implements IShopCarRpc {

    @Resource
    private IShopCarService iShopCarService;

    /**
     * 添加购物车商品
     * @param shopCarReqDto
     * @return
     */
    @Override
    public Boolean addCar(ShopCarReqDto shopCarReqDto) {
        return iShopCarService.addCar(shopCarReqDto);
    }

    @Override
    public ShopCarRespDto getCarInfo(ShopCarReqDto shopCarReqDto) {
        return iShopCarService.getCarInfo(shopCarReqDto);
    }

    @Override
    public Boolean removeFromCar(ShopCarReqDto shopCarReqDto) {
        return iShopCarService.removeFromCar(shopCarReqDto);
    }

    @Override
    public Boolean clearShopCar(ShopCarReqDto shopCarReqDto) {
        return iShopCarService.clearShopCar(shopCarReqDto);
    }

    @Override
    public Boolean addCarItemNum(ShopCarReqDto shopCarReqDto) {
        return iShopCarService.addCarItemNum(shopCarReqDto);
    }
}
