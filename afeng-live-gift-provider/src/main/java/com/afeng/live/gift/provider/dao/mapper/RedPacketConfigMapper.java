package com.afeng.live.gift.provider.dao.mapper;

import com.afeng.live.gift.provider.dao.po.RedPacketConfigPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RedPacketConfigMapper extends BaseMapper<RedPacketConfigPO> {

    @Update("update t_red_packet_config set total_get_price = total_get_price+#{price} where config_code = #{code}")
    void incrTotalGetPrice(String code, Integer price);

    @Update("update t_red_packet_config set total_get =  total_get+1 where config_code = #{code}")
    void incrTotalGet(String code);
}
