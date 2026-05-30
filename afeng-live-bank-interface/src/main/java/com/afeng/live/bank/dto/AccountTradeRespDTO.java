package com.afeng.live.bank.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountTradeRespDTO implements Serializable {
    private long userId;
    private boolean isSuccess;
    private String msg;
    private int code;

    public static AccountTradeRespDTO buildFail(long userId, String msg,int code){
        AccountTradeRespDTO accountTradeRespDTO = new AccountTradeRespDTO();
        accountTradeRespDTO.setUserId(userId);
        accountTradeRespDTO.setSuccess(false);
        accountTradeRespDTO.setMsg(msg);
        accountTradeRespDTO.setCode(code);
        return accountTradeRespDTO;
    }


    public static AccountTradeRespDTO buildSuccess(long userId, String msg){
        AccountTradeRespDTO accountTradeRespDTO = new AccountTradeRespDTO();
        accountTradeRespDTO.setUserId(userId);
        accountTradeRespDTO.setSuccess(true);
        accountTradeRespDTO.setMsg(msg);
        return accountTradeRespDTO;
    }
}
