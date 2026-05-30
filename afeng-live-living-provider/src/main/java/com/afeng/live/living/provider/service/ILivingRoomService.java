package com.afeng.live.living.provider.service;

import com.afeng.live.common.interfaces.dto.PageWrapper;
import com.afeng.live.im.core.server.interfaces.dto.ImOfflineDto;
import com.afeng.live.im.core.server.interfaces.dto.ImOnlineDto;
import com.afeng.living.interfaces.dto.LivingPkRespDTO;
import com.afeng.living.interfaces.dto.LivingRoomReqDTO;
import com.afeng.living.interfaces.dto.LivingRoomRespDTO;

import java.util.List;

public interface ILivingRoomService {

    /**
     * 开启直播间
     *
     * @param livingRoomReqDTO
     * @return
     */
    Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO);


    /**
     * 根据roomId查询直播间
     *
     * @param roomId
     * @return
     */
    LivingRoomRespDTO queryByRoomId(Integer roomId);

    /**
     * 直播间列表的分页查询
     *
     * @param livingRoomReqDTO
     * @return
     */
    PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDTO livingRoomReqDTO);


    /**
     * 查询所有的直播间类型
     *
     * @param type
     * @return
     */
    List<LivingRoomRespDTO> listAllLivingRoomFromDB(Integer type);


    /**
     * 用户上线处理
     *
     * @param imOnlineDto
     */
    void userOnlineHandler(ImOnlineDto imOnlineDto);


    /**
     * 用户或主播下线或意外断线处理
     *
     * @param imOfflineDto
     */
    void userOfflineHandler(ImOfflineDto imOfflineDto);

    /**
     * 查询直播间下的用户
     *根据roomId批量查询userId
     * @param livingRoomReqDTO
     * @return
     */
    List<Long> queryUserIdsByRoomId(LivingRoomReqDTO livingRoomReqDTO);

    /**
     * 根据roomId查询当前pk人是谁
     *
     * @param roomId
     * @return
     */
    Long queryOnlinePkUserId(Integer roomId);

    /**
     * 用户在pk直播间中，连上线请求
     *
     * @param livingRoomReqDTO
     * @return
     */
    LivingPkRespDTO onlinePk(LivingRoomReqDTO livingRoomReqDTO);


    /**
     * 用户在pk直播间中，下线请求
     *
     * @param livingRoomReqDTO
     * @return
     */
    boolean offlinePk(LivingRoomReqDTO livingRoomReqDTO);


    /**
     * 根据主播id查询直播间信息
     * @param anchorId
     * @return
     */
    LivingRoomRespDTO queryByAnchorId(Long anchorId);
}
