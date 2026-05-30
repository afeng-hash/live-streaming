package com.afeng.live.gift.provider.rpc;

import com.afeng.live.gift.dto.GiftConfigDto;
import com.afeng.live.gift.interfaces.IGiftConfigRpc;
import com.afeng.live.gift.provider.service.IGiftConfigService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class IGiftConfigRpcImpl implements IGiftConfigRpc {

    @Resource
    private IGiftConfigService iGiftConfigService;

    @Override
    public GiftConfigDto getByGifgId(Integer giftId) {
        return iGiftConfigService.getByGifgId(giftId);
    }

    @Override
    public List<GiftConfigDto> queryGiftList() {
        return iGiftConfigService.queryGiftList();
    }

    @Override
    public void insertOne(GiftConfigDto giftDto) {
        iGiftConfigService.insertOne(giftDto);
    }

    @Override
    public void updateOne(GiftConfigDto giftDto) {
        iGiftConfigService.updateOne(giftDto);
    }
}
