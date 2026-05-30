package com.afeng.live.bank.provider.service;


import com.afeng.live.bank.dto.AccountTradeReqDTO;
import com.afeng.live.bank.dto.AccountTradeRespDTO;
import com.afeng.live.bank.dto.AfengCurrencyAccountDTO;

/**
 * @Author idea
 * @Date: Created in 10:24 2023/8/6
 * @Description
 */
public interface IAfengCurrencyAccountService {

    /**
     * 新增账户
     *
     * @param userId
     */
    boolean insertOne(long userId);

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


    AfengCurrencyAccountDTO getByUserId(long userId);

    /**
     * 底层判断用户余额是否充足，充足则扣减，不足则拦截
     * @param accountTradeReqDTO
     * @return
     */
    AccountTradeRespDTO consume(AccountTradeReqDTO accountTradeReqDTO);

    /**
     * 专门给送礼业务调用的扣减余额逻辑
     * @param accountTradeReqDTO
     * @return
     */
    AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO);


    /**
     * 查询余额
     *
     * @param userId
     * @return
     */
    Integer getBalance(long userId);

    /**
     * 底层处理扣减余额逻辑
     * @param userId
     * @param num
     */
    public void consumeDBHandler(long userId,int num);


    /**
     * 底层处理增加余额逻辑
     * @param userId
     * @param num
     */
    public void consumeIncrDBHandler(long userId, int num);
}
