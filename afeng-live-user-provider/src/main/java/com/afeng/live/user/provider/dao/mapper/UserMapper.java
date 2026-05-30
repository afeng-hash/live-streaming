package com.afeng.live.user.provider.dao.mapper;

import com.afeng.live.user.provider.dao.po.UserPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
}
