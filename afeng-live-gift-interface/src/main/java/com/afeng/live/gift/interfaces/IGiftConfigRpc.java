package com.afeng.live.gift.interfaces;

import com.afeng.live.gift.dto.GiftConfigDto;

import java.util.List;

/**
 * 礼物接口
 */
public interface IGiftConfigRpc {

    /**
     * 根据礼物id查询礼物信息
     *
     * @param giftId
     * @return
     */
    GiftConfigDto getByGifgId(Integer giftId);

    /**
     * 查询礼物信息
     *
     * @return
     */
    List<GiftConfigDto> queryGiftList();

    /**
     * 插入一条礼物信息
     *
     * @param giftDto
     */
    void insertOne(GiftConfigDto giftDto);

    /**
     * 更新一条礼物信息
     *
     * @param giftDto
     */
    void updateOne(GiftConfigDto giftDto);
}
