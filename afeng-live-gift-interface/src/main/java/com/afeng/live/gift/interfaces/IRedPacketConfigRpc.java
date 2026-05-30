package com.afeng.live.gift.interfaces;

import com.afeng.live.gift.dto.RedPacketConfigReqDTO;
import com.afeng.live.gift.dto.RedPacketConfigRespDTO;
import com.afeng.live.gift.dto.RedPacketReceiveDTO;

public interface IRedPacketConfigRpc {

    /**
     * 根据主播id查询红包雨配置
     * @param anchordId
     * @return
     */
    RedPacketConfigRespDTO queryByAnchorId(Long anchordId);

    /**
     * 新增红包配置
     * @param redPacketConfigReqDTO
     * @return
     */
    boolean addOne(RedPacketConfigReqDTO redPacketConfigReqDTO);

    /**
     * 修改红包配置
     * @param redPacketConfigReqDTO
     * @return
     */
    boolean updateById(RedPacketConfigReqDTO redPacketConfigReqDTO);


    /**
     * 提前生成红包雨的数据
     * @param anchordId
     * @return
     */
    boolean prepareRedPacket(Long anchordId);

    /**
     * 领取红包
     * @param redPacketConfigReqDTO
     * @return
     */
    RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO redPacketConfigReqDTO);

    /**
     * 开始红包雨，通知直播间人
     * @param reqDTO
     * @return
     */
    Boolean startRedPacket(RedPacketConfigReqDTO reqDTO);
}
