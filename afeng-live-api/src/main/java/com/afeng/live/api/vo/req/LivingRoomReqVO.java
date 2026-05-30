package com.afeng.live.api.vo.req;

import lombok.Data;

/**
 * @Author idea
 * @Date: Created in 18:38 2023/7/23
 * @Description
 */
@Data
public class LivingRoomReqVO {

    private Integer type;
    private int page;
    private int pageSize;
    private Integer roomId;
    private String redPacketConfigCode;



    @Override
    public String toString() {
        return "LivingRoomReqVO{" +
                "type=" + type +
                ", page=" + page +
                ", pageSize=" + pageSize +
                '}';
    }
}
