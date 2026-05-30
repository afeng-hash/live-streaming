package com.afeng.live.bank.provider.service.impl;

import com.afeng.live.bank.provider.dao.mapper.IAfengCurrencyTradeMapper;
import com.afeng.live.bank.provider.dao.po.AfengCurrencyTradePO;
import com.afeng.live.bank.provider.service.IAfengCurrencyTradeService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AfengCurrencyTradeServiceImpl implements IAfengCurrencyTradeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfengCurrencyTradeServiceImpl.class);

    @Resource
    private IAfengCurrencyTradeMapper afengCurrencyTradeMapper;

    /**
     * 插入一条流水记录
     *
     * @param userId
     * @param num
     * @param type
     * @return
     */
    @Override
    public boolean insertOne(long userId, int num, int type) {
        try {
            AfengCurrencyTradePO tradePO = new AfengCurrencyTradePO();
            tradePO.setUserId(userId);
            tradePO.setNum(num);
            tradePO.setType(type);
            afengCurrencyTradeMapper.insert(tradePO);
            return true;
        } catch (Exception e) {
            LOGGER.error("[AfengCurrencyTradeServiceImpl] insert error is:", e);
        }
        return false;
    }
}
