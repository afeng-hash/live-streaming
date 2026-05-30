package com.afeng.live.gift.provider.rpc;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.common.interfaces.utils.ListUtils;
import com.afeng.live.framework.redis.starter.keys.GiftProviderCacheKeyBuilder;
import com.afeng.live.gift.dto.RedPacketConfigReqDTO;
import com.afeng.live.gift.dto.RedPacketConfigRespDTO;
import com.afeng.live.gift.dto.RedPacketReceiveDTO;
import com.afeng.live.gift.interfaces.IRedPacketConfigRpc;
import com.afeng.live.gift.provider.dao.po.RedPacketConfigPO;
import com.afeng.live.gift.provider.service.IRedPacketConfigService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class RedPacketConfigRpcImpl implements IRedPacketConfigRpc {

    @Resource
    private IRedPacketConfigService iRedPacketConfigService;


    @Override
    public RedPacketConfigRespDTO queryByAnchorId(Long anchordId) {
        return ConvertBeanUtils.convert(iRedPacketConfigService.queryByAnchorId(anchordId), RedPacketConfigRespDTO.class);
    }

    @Override
    public boolean addOne(RedPacketConfigReqDTO redPacketConfigReqDTO) {
        return iRedPacketConfigService.addOne(ConvertBeanUtils.convert(redPacketConfigReqDTO, RedPacketConfigPO.class));
    }

    @Override
    public boolean updateById(RedPacketConfigReqDTO redPacketConfigReqDTO) {
        return iRedPacketConfigService.updateById(ConvertBeanUtils.convert(redPacketConfigReqDTO, RedPacketConfigPO.class));
    }

    /**
     * 提前生成红包雨的数据
     * @param anchordId
     * @return
     */
    @Override
    public boolean prepareRedPacket(Long anchordId) {
        return iRedPacketConfigService.prepareRedPacket(anchordId);
    }

    /**
     * 领取红包
     * @param redPacketConfigReqDTO
     * @return
     */
    @Override
    public RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO redPacketConfigReqDTO) {
        return iRedPacketConfigService.receiveRedPacket( redPacketConfigReqDTO);
    }

    /**
     * 开始红包
     * @param reqDTO
     * @return
     */
    @Override
    public Boolean startRedPacket(RedPacketConfigReqDTO reqDTO) {
        return iRedPacketConfigService.startRedPacket(reqDTO);
    }


}
