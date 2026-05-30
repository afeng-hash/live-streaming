package com.afeng.live.bank.interfaces;

import com.afeng.live.bank.dto.AccountTradeReqDTO;
import com.afeng.live.bank.dto.AccountTradeRespDTO;

public interface IAfengCurrencyAccountRpc {
    /**
     * 增加虚拟币
     *
     * @param userId
     * @param num
     */
    void incr(long userId,int num);

    /**
     * 扣减虚拟币
     *
     * @param userId
     * @param num
     */
    void decr(long userId,int num);

    /**
     * 底层判断用户余额是否充足，充足则扣减，不足则拦截
     * @param accountTradeReqDTO
     * @return
     */
    AccountTradeRespDTO consume(AccountTradeReqDTO accountTradeReqDTO);

    /**
     * 查询余额
     *
     * @param userId
     * @return
     */
    Integer getBalance(long userId);

    /**
     * 底层判断用户余额是否充足，充足则扣减，不足则拦截
     * @param accountTradeReqDTO
     * @return
     */
    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO);
}
