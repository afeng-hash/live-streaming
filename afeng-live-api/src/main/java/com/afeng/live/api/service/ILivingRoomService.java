package com.afeng.live.api.service;


import com.afeng.live.api.vo.LivingRoomInitVO;
import com.afeng.live.api.vo.req.LivingRoomReqVO;
import com.afeng.live.api.vo.req.OnlinePkReqVO;
import com.afeng.live.api.vo.resp.LivingRoomPageRespVO;
import com.afeng.live.api.vo.resp.RedPacketRecevieVO;

/**
 * @Author idea
 * @Date: Created in 21:15 2023/7/19
 * @Description
 */
public interface ILivingRoomService {

    /**
     * 直播间列表展示
     *
     * @param livingRoomReqVO
     * @return
     */
    LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO);

    /**
     * 直播间列表展示
     *
     * @param livingRoomReqVO
     * @return
     */
//    LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO);

    /**
     * 开启直播间
     *
     * @param type
     */
    Integer startingLiving(Integer type);


    /**
     * 用户在pk直播间中，连上线请求
     *
     * @param onlinePkReqVO
     * @return
     */
//    boolean onlinePk(OnlinePkReqVO onlinePkReqVO);

    /**
     * 关闭直播间
     *
     * @param roomId
     * @return
     */
    boolean closeLiving(Integer roomId);

    /**
     * 根据用户id返回当前直播间相关信息
     *
     * @param userId
     * @param roomId
     * @return
     */
    LivingRoomInitVO anchorConfig(Long userId, Integer roomId);

    /**
     * 用户在pk直播间中，连上线请求
     *
     * @param onlinePkReqVO
     * @return
     */
    boolean onlinePk(OnlinePkReqVO onlinePkReqVO);

    /**
     * 初始化红包数据
     * @param userId
     * @return
     */
    Boolean prepareRedPacket(Long userId,Integer roomId);

    /**
     * 开始红包雨
     * @param userId
     * @param code
     * @return
     */
    Boolean startRedPacket(Long userId, String code);

    /**
     * 获取红包
     * @param userId
     * @param code
     * @return
     */
    RedPacketRecevieVO getRedPacket(Long userId, String code);
}
