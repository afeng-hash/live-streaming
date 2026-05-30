package com.afeng.live.bank.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountTradeReqDTO implements Serializable {

    private long userId;
    private int num;
}
