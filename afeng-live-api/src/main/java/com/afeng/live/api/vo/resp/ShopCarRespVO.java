package com.afeng.live.api.vo.resp;

import com.afeng.live.gift.dto.ShopCarItemRespDto;
import lombok.Data;

import java.util.List;

@Data
public class ShopCarRespVO {
    private Long userId;
    private Integer roomId;
    private List<ShopCarItemRespDto> shopCarItemRespDtoList;
}
