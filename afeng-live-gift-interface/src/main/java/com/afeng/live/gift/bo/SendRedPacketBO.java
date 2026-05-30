package com.afeng.live.gift.bo;

import com.afeng.live.gift.dto.RedPacketConfigReqDTO;
import lombok.Data;

@Data
public class SendRedPacketBO {

    private RedPacketConfigReqDTO reqDTO;
    private Integer price;
}
