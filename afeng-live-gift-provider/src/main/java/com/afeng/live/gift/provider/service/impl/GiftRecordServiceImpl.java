package com.afeng.live.gift.provider.service.impl;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.gift.dto.GiftRecordDTO;
import com.afeng.live.gift.provider.dao.mapper.GiftRecordMapper;
import com.afeng.live.gift.provider.dao.po.GiftRecordPO;
import com.afeng.live.gift.provider.service.IGiftRecordService;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

/**
 * @Author idea
 * @Date: Created in 15:11 2023/7/30
 * @Description
 */
@Service
public class GiftRecordServiceImpl implements IGiftRecordService {

    @Resource
    private GiftRecordMapper giftRecordMapper;

    @Override
    public void insertOne(GiftRecordDTO giftRecordDTO) {
        GiftRecordPO giftRecordPO = ConvertBeanUtils.convert(giftRecordDTO,GiftRecordPO.class);
        giftRecordMapper.insert(giftRecordPO);
    }
}
