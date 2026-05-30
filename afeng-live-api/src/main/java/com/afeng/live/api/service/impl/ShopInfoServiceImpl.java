package com.afeng.live.api.service.impl;

import com.afeng.live.api.service.IShopInfoService;
import com.afeng.live.api.vo.req.ShopCarReqVO;
import com.afeng.live.api.vo.req.SkuInfoReqVO;
import com.afeng.live.api.vo.resp.ShopCarRespVO;
import com.afeng.live.api.vo.resp.SkuDetailInfoVO;
import com.afeng.live.api.vo.resp.SkuInfoVo;
import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.gift.dto.ShopCarReqDto;
import com.afeng.live.gift.dto.ShopCarRespDto;
import com.afeng.live.gift.dto.SkuInfoDto;
import com.afeng.live.gift.interfaces.IShopCarRpc;
import com.afeng.live.gift.interfaces.ISkuInfoRpc;
import com.afeng.live.web.starter.error.BizBaseErrorEnum;
import com.afeng.live.web.starter.error.ErrorAssert;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import com.afeng.living.interfaces.dto.LivingRoomRespDTO;
import com.afeng.living.interfaces.rpc.ILivingRoomRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
public class ShopInfoServiceImpl implements IShopInfoService {

    @DubboReference
    private ILivingRoomRpc iLivingRoomRpc;
    @DubboReference
    private ISkuInfoRpc iSkuInfoRpc;
    @DubboReference
    private IShopCarRpc iShopCarRpc;

    @Override
    public List<SkuInfoVo> queryByRoomId(Integer roomId) {
        LivingRoomRespDTO livingRoomRespDTO = iLivingRoomRpc.queryByRoomId(roomId);
        ErrorAssert.isNotNull(livingRoomRespDTO, BizBaseErrorEnum.PARAM_ERROR);
        Long anchorId = livingRoomRespDTO.getAnchorId();
        List<SkuInfoDto> skuInfoDtos = iSkuInfoRpc.queryByAnchorId(anchorId);
        return ConvertBeanUtils.convertList(skuInfoDtos, SkuInfoVo.class);
    }

    /**
     * 商品详情
     *
     * @param skuInfoReqVO
     * @return
     */
    @Override
    public SkuDetailInfoVO detail(SkuInfoReqVO skuInfoReqVO) {
        return ConvertBeanUtils.convert(iSkuInfoRpc.queryBySkuId(skuInfoReqVO.getSkuId()), SkuDetailInfoVO.class);
    }


    /**
     * 添加购物车商品
     * @param shopCarReqVO
     * @return
     */
    @Override
    public Boolean addCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDto shopCarReqDto = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDto.class);
        shopCarReqDto.setUserId(AfengRequestContext.getUserId());
        return iShopCarRpc.addCar(shopCarReqDto);
    }

    /**
     * 获取购物车商品
     * @param shopCarReqVO
     * @return
     */
    @Override
    public ShopCarRespVO getCarInfo(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDto shopCarReqDto = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDto.class);
        shopCarReqDto.setUserId(AfengRequestContext.getUserId());
        return ConvertBeanUtils.convert(iShopCarRpc.getCarInfo(shopCarReqDto), ShopCarRespVO.class);
    }

    /**
     * 移除购物车商品
     * @param shopCarReqVO
     * @return
     */
    @Override
    public Boolean removeFromCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDto shopCarReqDto = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDto.class);
        shopCarReqDto.setUserId(AfengRequestContext.getUserId());
        return iShopCarRpc.removeFromCar(shopCarReqDto);
    }

    /**
     * 清空购物车
     * @param shopCarReqVO
     * @return
     */
    @Override
    public Boolean clearShopCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDto shopCarReqDto = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDto.class);
        shopCarReqDto.setUserId(AfengRequestContext.getUserId());
        return iShopCarRpc.clearShopCar(shopCarReqDto);
    }

    /**
     * 修改商品数量
     * @param shopCarReqVO
     * @return
     */
    @Override
    public Boolean addCarItemNum(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDto shopCarReqDto = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDto.class);
        shopCarReqDto.setUserId(AfengRequestContext.getUserId());
        return iShopCarRpc.addCarItemNum(shopCarReqDto);
    }


}
