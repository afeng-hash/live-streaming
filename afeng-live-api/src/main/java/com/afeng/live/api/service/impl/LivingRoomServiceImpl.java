package com.afeng.live.api.service.impl;

import com.afeng.live.api.error.ApiErrorEnum;
import com.afeng.live.api.service.ILivingRoomService;
import com.afeng.live.api.vo.LivingRoomInitVO;
import com.afeng.live.api.vo.req.LivingRoomReqVO;
import com.afeng.live.api.vo.req.OnlinePkReqVO;
import com.afeng.live.api.vo.resp.LivingRoomPageRespVO;
import com.afeng.live.api.vo.resp.LivingRoomRespVO;
import com.afeng.live.api.vo.resp.RedPacketRecevieVO;
import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.common.interfaces.dto.PageWrapper;
import com.afeng.live.gift.dto.RedPacketConfigReqDTO;
import com.afeng.live.gift.dto.RedPacketConfigRespDTO;
import com.afeng.live.gift.dto.RedPacketReceiveDTO;
import com.afeng.live.gift.interfaces.IRedPacketConfigRpc;
import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.user.dto.UserDto;
import com.afeng.live.user.interfaces.IUserRpc;
import com.afeng.live.web.starter.error.BizBaseErrorEnum;
import com.afeng.live.web.starter.error.ErrorAssert;
import com.afeng.live.web.starter.error.AfengErrorException;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import com.afeng.living.interfaces.dto.LivingPkRespDTO;
import com.afeng.living.interfaces.dto.LivingRoomReqDTO;
import com.afeng.living.interfaces.dto.LivingRoomRespDTO;
import com.afeng.living.interfaces.rpc.ILivingRoomRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author idea
 * @Date: Created in 21:15 2023/7/19
 * @Description
 */
@Service
public class LivingRoomServiceImpl implements ILivingRoomService {

    @DubboReference
    private IUserRpc userRpc;
    @DubboReference
    private ILivingRoomRpc livingRoomRpc;
    @Resource
    private IRedPacketConfigRpc iRedPacketConfigRpc;


    /**
     * 直播间列表
     *
     * @param livingRoomReqVO
     * @return
     */
    @Override
    public LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO) {
        PageWrapper<LivingRoomRespDTO> resultPage = livingRoomRpc.list(ConvertBeanUtils.convert(livingRoomReqVO,LivingRoomReqDTO.class));
        LivingRoomPageRespVO livingRoomPageRespVO = new LivingRoomPageRespVO();
        livingRoomPageRespVO.setList(ConvertBeanUtils.convertList(resultPage.getList(), LivingRoomRespVO.class));
        livingRoomPageRespVO.setHasNext(resultPage.isHasNext());
        return livingRoomPageRespVO;
    }


    /**
     * 开启直播间
     *
     * @param type
     * @return
     */
    @Override
    public Integer startingLiving(Integer type) {
        Long userId = AfengRequestContext.getUserId();
        UserDto userDTO = userRpc.getUserById(userId);
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setAnchorId(userId);
        livingRoomReqDTO.setRoomName("主播-" + AfengRequestContext.getUserId() + "的直播间");
        livingRoomReqDTO.setCovertImg(userDTO.getAvatar());
        livingRoomReqDTO.setType(type);
        return livingRoomRpc.startLivingRoom(livingRoomReqDTO);
    }


    /**
     * 关闭直播间
     *
     * @param roomId
     * @return
     */
    @Override
    public boolean closeLiving(Integer roomId) {
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(roomId);
        livingRoomReqDTO.setAnchorId(AfengRequestContext.getUserId());
        return livingRoomRpc.closeLiving(livingRoomReqDTO);
    }

    /**
     * 获取主播的配置信息
     *
     * @param userId
     * @param roomId
     * @return
     */
    @Override
    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
        UserDto userDto = userRpc.getUserById(userId);
        ErrorAssert.isNotNull(respDTO, ApiErrorEnum.LIVING_ROOM_END);
        Map<Long,UserDto> userDTOMap = userRpc.batchQueryUserByIds(Arrays.asList(respDTO.getAnchorId(),userId).stream().distinct().collect(Collectors.toList()));
        UserDto anchor = userDTOMap.get(respDTO.getAnchorId());
        UserDto watcher = userDTOMap.get(userId);
        LivingRoomInitVO respVO = new LivingRoomInitVO();
//        respVO.setAnchorNickName(userDto.getNickName());
        respVO.setAnchorNickName(anchor.getNickName());
        respVO.setWatcherNickName(watcher.getNickName());
        respVO.setUserId(userId);
        //给定一个默认的头像
        respVO.setAvatar(StringUtils.isEmpty(userDto.getAvatar())?"https://s1.ax1x.com/2022/12/18/zb6q6f.png":userDto.getAvatar());
        respVO.setWatcherAvatar(watcher.getAvatar());
        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
            //这种就是属于直播间已经不存在的情况了
            respVO.setRoomId(-1);
            return respVO;
        }
        boolean isAnchor = respDTO.getAnchorId().equals(userId);
        respVO.setRoomId(respDTO.getId());
        respVO.setAnchorId(respDTO.getAnchorId());
        respVO.setAnchor(isAnchor);
        if (isAnchor){
            RedPacketConfigRespDTO redPacketConfigRespDTO = iRedPacketConfigRpc.queryByAnchorId(userId);
            if (redPacketConfigRespDTO != null){
                respVO.setRedPacketConfigCode(redPacketConfigRespDTO.getConfigCode());
            }
        }
        respVO.setDefaultBgImg("https://picst.sunbangyan.cn/2023/08/29/waxzj0.png");
        return respVO;
    }



    @Override
    public boolean onlinePk(OnlinePkReqVO onlinePkReqVO) {
        LivingRoomReqDTO reqDTO = ConvertBeanUtils.convert(onlinePkReqVO,LivingRoomReqDTO.class);
        reqDTO.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
        reqDTO.setPkObjId(AfengRequestContext.getUserId());
        LivingPkRespDTO tryOnlineStatus = livingRoomRpc.onlinePk(reqDTO);
        ErrorAssert.isTure(tryOnlineStatus.isOnlineStatus(), new AfengErrorException(-1,tryOnlineStatus.getMsg()));
        return true;
    }

    /**
     * 初始化红包数据
     * @param userId
     * @return
     */
    @Override
    public Boolean prepareRedPacket(Long userId,Integer roomId) {
        LivingRoomRespDTO livingRoomRespDTO = livingRoomRpc.queryByRoomId(roomId);
        ErrorAssert.isNotNull(livingRoomRespDTO, BizBaseErrorEnum.PARAM_ERROR);
        //判断当前用户是不是主播
        ErrorAssert.isNotNull(livingRoomRespDTO.getAnchorId().equals(userId),BizBaseErrorEnum.PARAM_ERROR);
        return iRedPacketConfigRpc.prepareRedPacket(userId);
    }

    /**
     * 开始红包
     * @param userId
     * @param code
     * @return
     */
    @Override
    public Boolean startRedPacket(Long userId, String code) {
        RedPacketConfigReqDTO reqDTO = new RedPacketConfigReqDTO();
        reqDTO.setUserId(userId);
        reqDTO.setRedPacketConfigCode(code);
        LivingRoomRespDTO livingRoomRespDTO = livingRoomRpc.queryByAnchorId(userId);
        ErrorAssert.isNotNull(livingRoomRespDTO,BizBaseErrorEnum.PARAM_ERROR);
        reqDTO.setRoomId(livingRoomRespDTO.getId());
        return iRedPacketConfigRpc.startRedPacket(reqDTO);
    }

    /**
     * 领取红包
     * @param userId
     * @param code
     * @return
     */
    @Override
    public RedPacketRecevieVO getRedPacket(Long userId, String code) {
        RedPacketConfigReqDTO reqDTO = new RedPacketConfigReqDTO();
        reqDTO.setUserId(userId);
        reqDTO.setRedPacketConfigCode(code);
        RedPacketReceiveDTO redPacketReceiveDTO = iRedPacketConfigRpc.receiveRedPacket(reqDTO);
        RedPacketRecevieVO res = new RedPacketRecevieVO();
        if (redPacketReceiveDTO == null){
            res.setMsg("红包派发完毕");
        }else{
            res.setPrice(redPacketReceiveDTO.getPrice());
        }
        return res;
    }

}
