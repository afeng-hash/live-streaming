package com.afeng.live.living.provider.service;


import com.afeng.living.interfaces.dto.LivingRoomReqDTO;

/**
 * 直播间事务处理
 *
 * @Author idea
 * @Date: Created in 19:21 2023/8/29
 * @Description
 */
public interface ILivingRoomTxService {

    /**
     * 关闭直播间
     *
     * @param livingRoomReqDTO
     * @return
     */
    boolean closeLiving(LivingRoomReqDTO livingRoomReqDTO);

}
