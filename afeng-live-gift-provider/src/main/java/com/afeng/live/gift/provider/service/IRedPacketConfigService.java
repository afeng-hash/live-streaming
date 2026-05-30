package com.afeng.live.gift.provider.service;

import com.afeng.live.gift.dto.RedPacketConfigReqDTO;
import com.afeng.live.gift.dto.RedPacketReceiveDTO;
import com.afeng.live.gift.provider.dao.po.RedPacketConfigPO;

public interface IRedPacketConfigService {

    /**
     * 根据主播id查询红包雨配置
     * @param anchordId
     * @return
     */
    RedPacketConfigPO queryByAnchorId(Long anchordId);


    /**
     * 新增红包配置
     * @param redPacketConfigPO
     * @return
     */
    boolean addOne(RedPacketConfigPO redPacketConfigPO);

    /**
     * 修改红包配置
     * @param redPacketConfigPO
     * @return
     */
    boolean updateById(RedPacketConfigPO redPacketConfigPO);

    /**
     * 提前生成红包雨的数据
     * @param anchordId
     * @return
     */
    public boolean prepareRedPacket(Long anchordId);

    /**
     * 领取红包
     * @param redPacketConfigReqDTO
     * @return
     */
    RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO redPacketConfigReqDTO);

    /**
     * 开始红包
     * @param reqDTO
     * @return
     */
    Boolean startRedPacket(RedPacketConfigReqDTO reqDTO);


    /**
     * 根据配置码查询红包配置
     * @param configCode
     * @return
     */
    RedPacketConfigPO queryByConfigCode(String configCode);

    /**
     * 处理红包领取
     * @param reqDTO
     * @param price
     */
    void receiveRedPacketHandle(RedPacketConfigReqDTO reqDTO,Integer price);
}
