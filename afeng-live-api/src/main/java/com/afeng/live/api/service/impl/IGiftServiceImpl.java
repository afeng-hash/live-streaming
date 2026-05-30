package com.afeng.live.api.service.impl;

import com.afeng.live.api.error.ApiErrorEnum;
import com.afeng.live.api.service.IGiftService;
import com.afeng.live.api.vo.req.GiftReqVO;
import com.afeng.live.api.vo.resp.GiftConfigVO;
import com.afeng.live.bank.dto.AccountTradeReqDTO;
import com.afeng.live.bank.dto.AccountTradeRespDTO;
import com.afeng.live.bank.interfaces.IAfengCurrencyAccountRpc;
import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.common.interfaces.common.GiftProviderTopicNames;
import com.afeng.live.common.interfaces.dto.SendGiftMq;
import com.afeng.live.gift.dto.GiftConfigDto;
import com.afeng.live.gift.interfaces.IGiftConfigRpc;
import com.afeng.live.web.starter.error.ErrorAssert;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Service
public class IGiftServiceImpl implements IGiftService {

    @DubboReference
    private IGiftConfigRpc giftConfigRpc;
    @DubboReference
    private IAfengCurrencyAccountRpc afengCurrencyAccountRpc;
    @Resource
    private MQProducer mqProducer;
    private Cache<Integer, GiftConfigDto> giftConfigDTOCache = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(90, TimeUnit.SECONDS).build();


    @Override
    public List<GiftConfigVO> listGift() {
        List<GiftConfigDto> giftConfigDtos = giftConfigRpc.queryGiftList();
        return ConvertBeanUtils.convertList(giftConfigDtos, GiftConfigVO.class);
    }

    /**
     * 送礼
     *
     * @param giftReqVO
     * @return
     */
    @Override
    public boolean send(GiftReqVO giftReqVO) {
        int giftId = giftReqVO.getGiftId();
        ErrorAssert.isNotNull(giftId, ApiErrorEnum.GIFT_CONFIG_ERROR);
        //mapper集合，判断是否本地有对象，如果有就返回，如果没有就rpc调用，同时注入到本地map中
        GiftConfigDto giftConfigDto = giftConfigDTOCache.get(giftId, integer -> giftConfigRpc.getByGifgId(giftId));

        ErrorAssert.isNotNull(giftConfigDto, ApiErrorEnum.GIFT_CONFIG_ERROR);
        ErrorAssert.isTure(giftConfigDto.getStatus() == 1, ApiErrorEnum.GIFT_CONFIG_ERROR);

        SendGiftMq sendGiftMq = new SendGiftMq();
        sendGiftMq.setUserId(AfengRequestContext.getUserId());
        sendGiftMq.setGiftId(giftId);
        sendGiftMq.setRoomId(giftReqVO.getRoomId());
        sendGiftMq.setReceiverId(giftReqVO.getReceiverId());
        sendGiftMq.setUrl(giftConfigDto.getSvgaUrl());
        sendGiftMq.setType(giftReqVO.getType());
        sendGiftMq.setPrice(giftConfigDto.getPrice());
        //避免重复消费
        sendGiftMq.setUuid(UUID.randomUUID().toString());
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.SEND_GIFT);
        message.setBody(JSON.toJSONBytes(sendGiftMq));
        try {
            SendResult sendResult = mqProducer.send(message);
            log.info("[gift-send] send result is {}", sendResult);
        } catch (Exception e) {
            log.info("[gift-send] send result is error:", e);
        }
        return true;
    }
}
