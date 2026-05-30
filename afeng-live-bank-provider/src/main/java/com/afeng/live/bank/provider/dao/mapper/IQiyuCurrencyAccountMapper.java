package com.afeng.live.bank.provider.dao.mapper;

import com.afeng.live.bank.provider.dao.po.AfengCurrencyAccountPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 虚拟币账户mapper
 *
 * @Author idea
 * @Date: Created in 10:24 2023/8/6
 * @Description
 */
@Mapper
public interface IAfengCurrencyAccountMapper extends BaseMapper<AfengCurrencyAccountPO> {

    @Update("update t_afeng_currency_account set current_balance = current_balance + #{num} where user_id = #{userId}")
    void incr(@Param("userId") long userId,@Param("num") int num);

    @Select("select current_balance from t_afeng_currency_account where user_id=#{userId} and status = 1 limit 1")
    Integer queryBalance(@Param("userId") long userId);

    @Update("update t_afeng_currency_account set current_balance = current_balance - #{num} where user_id = #{userId}")
    void decr(@Param("userId") long userId,@Param("num") int num);


}
