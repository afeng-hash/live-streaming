package com.afeng.live.api.controller;

import com.afeng.live.api.service.ILivingRoomService;
import com.afeng.live.api.vo.LivingRoomInitVO;
import com.afeng.live.api.vo.req.LivingRoomReqVO;
import com.afeng.live.api.vo.req.OnlinePkReqVO;
import com.afeng.live.common.interfaces.vo.WebResponseVO;
import com.afeng.live.web.starter.error.BizBaseErrorEnum;
import com.afeng.live.web.starter.error.ErrorAssert;
import com.afeng.live.web.starter.limit.RequestLimit;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/living")
public class LivingRoomController {

    @Resource
    private ILivingRoomService livingRoomService;

    /**
     * 是否有红包雨权限
     * @param livingRoomReqVO
     * @return
     */
    @PostMapping("/prepareRedPacket")
    @RequestLimit(limit = 1, second = 10, msg = "正在初始化中请求过于频繁，请稍后再试")
    public WebResponseVO prepareRedPacket(LivingRoomReqVO livingRoomReqVO){
        return WebResponseVO.success(livingRoomService.prepareRedPacket(AfengRequestContext.getUserId(),livingRoomReqVO.getRoomId()));
    }

    /**
     * 开启红包雨
     * @param livingRoomReqVO
     * @return
     */
    @PostMapping("/startRedPacket")
    @RequestLimit(limit = 1, second = 10, msg = "正在通知直播间中，请稍后再试")
    public WebResponseVO startRedPacket(LivingRoomReqVO livingRoomReqVO){
        return WebResponseVO.success(livingRoomService.startRedPacket(AfengRequestContext.getUserId(),livingRoomReqVO.getRedPacketConfigCode()));
    }

    /**
     * 领取红包
     * @param livingRoomReqVO
     * @return
     */
    @PutMapping("/getRedPacket")
    @RequestLimit(limit = 1, second = 3, msg = "")
    public WebResponseVO getRedPacket(LivingRoomReqVO livingRoomReqVO){
        return WebResponseVO.success(livingRoomService.getRedPacket(AfengRequestContext.getUserId(),livingRoomReqVO.getRedPacketConfigCode()));
    }


    /**
     * 开播
     *
     * @param type
     * @return
     */
    @RequestLimit(limit = 1, second = 10, msg = "开播请求过于频繁，请稍后再试")
    @PostMapping("/startingLiving")
    public WebResponseVO startingLiving(Integer type) {
        ErrorAssert.isNotNull(type, BizBaseErrorEnum.PARAM_ERROR);
        Integer roomId = livingRoomService.startingLiving(type);
        LivingRoomInitVO initVO = new LivingRoomInitVO();
        initVO.setRoomId(roomId);
        return WebResponseVO.success(initVO);
    }

    /**
     * 关播
     *
     * @param roomId
     * @return
     */
    @RequestLimit(limit = 1, second = 10, msg = "关播请求过于频繁，请稍后再试")
    @PostMapping("/closeLiving")
    public WebResponseVO closeLiving(Integer roomId) {
        ErrorAssert.isNotNull(roomId, BizBaseErrorEnum.PARAM_ERROR);
        boolean closeStatus = livingRoomService.closeLiving(roomId);
        if (closeStatus) {
            return WebResponseVO.success();
        }
        return WebResponseVO.bizError("关播异常");
    }

    /**
     * 获取主播相关配置信息（只有主播才会有权限）
     *
     * @return
     */
    @PostMapping("/anchorConfig")
    public WebResponseVO anchorConfig(Integer roomId) {
        return WebResponseVO.success(livingRoomService.anchorConfig(AfengRequestContext.getUserId(), roomId));
    }


    /**
     * 直播间列表
     *
     * @param livingRoomReqVO
     * @return
     */
    @PostMapping("/list")
    public WebResponseVO list(LivingRoomReqVO livingRoomReqVO) {
        log.info("分页查询直播间");
        ErrorAssert.isTure(livingRoomReqVO != null && livingRoomReqVO.getType() != null, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isTure(livingRoomReqVO.getPage() > 0 && livingRoomReqVO.getPageSize() <= 100, BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(livingRoomService.list(livingRoomReqVO));
    }


    /**
     * 用户请求pk
     *
     * @param onlinePkReqVO
     * @return
     */
    @PostMapping("/onlinePk")
    @RequestLimit(limit = 1,second = 3)
    public WebResponseVO onlinePk(OnlinePkReqVO onlinePkReqVO) {
        ErrorAssert.isNotNull(onlinePkReqVO.getRoomId(), BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(livingRoomService.onlinePk(onlinePkReqVO));
    }
}
