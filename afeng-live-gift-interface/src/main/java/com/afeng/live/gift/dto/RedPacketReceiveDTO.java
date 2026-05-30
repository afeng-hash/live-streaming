package com.afeng.live.gift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class RedPacketReceiveDTO implements Serializable {

    private Integer price;
    private Boolean status;
}
