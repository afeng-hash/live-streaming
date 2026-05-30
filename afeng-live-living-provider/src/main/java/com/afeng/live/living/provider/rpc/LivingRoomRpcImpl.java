package com.afeng.live.living.provider.rpc;

import com.afeng.live.common.interfaces.dto.PageWrapper;
import com.afeng.live.im.core.server.interfaces.dto.ImOfflineDto;
import com.afeng.live.im.core.server.interfaces.dto.ImOnlineDto;
import com.afeng.live.living.provider.service.ILivingRoomService;
import com.afeng.live.living.provider.service.ILivingRoomTxService;
import com.afeng.living.interfaces.dto.LivingPkRespDTO;
import com.afeng.living.interfaces.dto.LivingRoomReqDTO;
import com.afeng.living.interfaces.dto.LivingRoomRespDTO;
import com.afeng.living.interfaces.rpc.ILivingRoomRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class LivingRoomRpcImpl implements ILivingRoomRpc {

    @Resource
    private ILivingRoomService livingRoomService;

    @Resource
    private ILivingRoomTxService livingRoomTxService;

    /**
     * 查询直播间下的用户
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public List<Long> queryUserIdsByRoomId(LivingRoomReqDTO livingRoomReqDTO) {
        return livingRoomService.queryUserIdsByRoomId(livingRoomReqDTO);
    }

    /**
     * 直播间列表的分页查询
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDTO livingRoomReqDTO) {
        return livingRoomService.list(livingRoomReqDTO);
    }

    /**
     * 根据roomId查询直播间
     *
     * @param roomId
     * @return
     */
    @Override
    public LivingRoomRespDTO queryByRoomId(Integer roomId) {
        return livingRoomService.queryByRoomId(roomId);
    }


    /**
     * 开启直播间
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO) {
        return livingRoomService.startLivingRoom(livingRoomReqDTO);
    }

    /**
     * 直播间列表的分页查询
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public boolean closeLiving(LivingRoomReqDTO livingRoomReqDTO) {
        return livingRoomTxService.closeLiving(livingRoomReqDTO);
    }

    /**
     * 用户上线处理
     *
     * @param livingRoomReqDTO
     */
    @Override
    public LivingPkRespDTO onlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        return livingRoomService.onlinePk(livingRoomReqDTO);
    }

    /**
     * 根据roomId查询当前pk人是谁
     *
     * @param roomId
     * @return
     */
    @Override
    public Long queryOnlinePkUserId(Integer roomId) {
        return livingRoomService.queryOnlinePkUserId(roomId);
    }

    /**
     * 用户在pk直播间中，下线请求
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public boolean offlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        return livingRoomService.offlinePk(livingRoomReqDTO);
    }


    /**
     * 根据主播id查询直播间信息
     * @param anchorId
     * @return
     */
    @Override
    public LivingRoomRespDTO queryByAnchorId(Long anchorId) {
        return livingRoomService.queryByAnchorId(anchorId);
    }


}
