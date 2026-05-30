package com.afeng.live.msg.provider.consumer.handler.impl;

import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.router.interfaces.rpc.ImRouterRpc;
import com.afeng.live.msg.interfaces.dto.MessageDto;
import com.afeng.live.msg.provider.consumer.handler.MessageHandler;
import com.afeng.living.interfaces.dto.LivingRoomReqDTO;
import com.afeng.living.interfaces.rpc.ILivingRoomRpc;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import com.afeng.live.im.router.interfaces.constants.ImMsgBizCodeEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SingleMessageHandler implements MessageHandler {

    @DubboReference
    private ImRouterRpc imRouterRpc;
    @DubboReference
    private ILivingRoomRpc iLivingRoomRpc;

    /**
     * 接收到消息处理逻辑
     * @param imMsgBody
     */
    @Override
    public void onMsgReceive(ImMsgBody imMsgBody) {
        int bizCode = imMsgBody.getBizCode();
        if (bizCode == ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode()){
            //直播间聊天消息
            log.info("收到直播间聊天消息：{}", imMsgBody.getData());
            MessageDto messageDto = JSON.parseObject(imMsgBody.getData(), MessageDto.class);
            Integer roomId = messageDto.getRoomId();

            LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
            livingRoomReqDTO.setAppId(imMsgBody.getAppId());
            livingRoomReqDTO.setRoomId(roomId);
            List<Long> userIdList = iLivingRoomRpc.queryUserIdsByRoomId(livingRoomReqDTO);
            userIdList = userIdList.stream().filter(userId -> !userId.equals(imMsgBody.getUserId())).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(userIdList)){
                return;
            }

            List<ImMsgBody> imMsgBodyList = userIdList.stream().map(userId -> {
                //发送消息给目标用户
                ImMsgBody responseImMsgBody = new ImMsgBody();
                responseImMsgBody.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
                responseImMsgBody.setUserId(userId);
                responseImMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode());
                responseImMsgBody.setData(JSON.toJSONString(messageDto));
                return responseImMsgBody;
            }).collect(Collectors.toList());

           log.info("[SingleMessageHandler] 批量发送消息给目标用户：{}", imMsgBodyList);
            //批量发送消息给目标用户
            imRouterRpc.batchSendMsg(imMsgBodyList);

        }
    }
}
