package com.afeng.live.bank.provider.rpc;

import com.afeng.live.bank.dto.AccountTradeReqDTO;
import com.afeng.live.bank.dto.AccountTradeRespDTO;
import com.afeng.live.bank.interfaces.IAfengCurrencyAccountRpc;
import com.afeng.live.bank.provider.service.IAfengCurrencyAccountService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


/**
 * @Author idea
 * @Date: Created in 10:28 2023/8/6
 * @Description
 */
@DubboService
public class AfengCurrencyAccountRpcImpl implements IAfengCurrencyAccountRpc {

    @Resource
    private IAfengCurrencyAccountService afengCurrencyAccountService;

    @Override
    public void incr(long userId, int num) {
        afengCurrencyAccountService.incr(userId, num);
    }

    @Override
    public void decr(long userId, int num) {
        afengCurrencyAccountService.decr(userId, num);
    }

    /**
     * 底层判断用户余额是否充足，充足则扣减，不足则拦截
     * @param accountTradeReqDTO
     * @return
     */
    @Override
    public AccountTradeRespDTO consume(AccountTradeReqDTO accountTradeReqDTO) {
        return afengCurrencyAccountService.consume(accountTradeReqDTO);
    }

    @Override
    public Integer getBalance(long userId) {
        return afengCurrencyAccountService.getBalance(userId);
    }

    @Override
    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO) {
        return afengCurrencyAccountService.consumeForSendGift(accountTradeReqDTO);
    }

}
