package com.afeng.live.gift.provider.service.impl;

import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.gift.provider.dao.mapper.AnchorShopInfoMapper;
import com.afeng.live.gift.provider.dao.po.AnchorShopInfoPO;
import com.afeng.live.gift.provider.service.IAnchorShopInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnchorShopInfoServiceImpl implements IAnchorShopInfoService {

    @Resource
    private AnchorShopInfoMapper anchorShopInfoMapper;

    /**
     * 根据主播id查询skuId信息
     * @param anchorId
     * @return
     */
    @Override
    public List<Long> querySkuIdByAnchorId(Long anchorId) {
        LambdaQueryWrapper<AnchorShopInfoPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AnchorShopInfoPO::getAnchorId,anchorId);
        lambdaQueryWrapper.eq(AnchorShopInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        return anchorShopInfoMapper.selectList(lambdaQueryWrapper).stream().map(AnchorShopInfoPO::getSkuId).collect(Collectors.toList());
    }

    /**
     * 查询所有有效的主播id
     * @return
     */
    @Override
    public List<Long> queryAllValidAnchorId() {
        LambdaQueryWrapper<AnchorShopInfoPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AnchorShopInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        return anchorShopInfoMapper.selectList(lambdaQueryWrapper).stream().map(AnchorShopInfoPO::getSkuId).collect(Collectors.toList());
    }
}
