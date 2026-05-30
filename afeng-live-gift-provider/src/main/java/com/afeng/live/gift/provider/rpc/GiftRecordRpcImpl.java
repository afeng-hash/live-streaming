package com.afeng.live.gift.provider.rpc;

import com.afeng.live.gift.dto.GiftRecordDTO;
import com.afeng.live.gift.interfaces.IGiftRecordRpc;
import com.afeng.live.gift.provider.service.IGiftRecordService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


/**
 * 送礼记录
 */
@DubboService
public class GiftRecordRpcImpl implements IGiftRecordRpc {

    @Resource
    private IGiftRecordService giftRecordService;

    @Override
    public void insertOne(GiftRecordDTO giftRecordDTO) {
        giftRecordService.insertOne(giftRecordDTO);
    }
}
