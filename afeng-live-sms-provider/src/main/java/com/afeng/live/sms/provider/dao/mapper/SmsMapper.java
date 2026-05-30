package com.afeng.live.sms.provider.dao.mapper;

import com.afeng.live.sms.provider.dao.po.SmsPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsMapper extends BaseMapper<SmsPO> {
}