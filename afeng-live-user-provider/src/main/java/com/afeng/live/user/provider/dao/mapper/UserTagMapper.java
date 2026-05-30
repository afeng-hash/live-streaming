package com.afeng.live.user.provider.dao.mapper;

import com.afeng.live.user.provider.dao.po.UserTagPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserTagMapper extends BaseMapper<UserTagPO> {

    /**
     * 设置标签
     * #{} - 预编译参数：
     *       防止SQL注入：参数会被转义处理
     *      类型安全：自动进行类型转换
     *      性能优化：数据库可以缓存执行计划
     *      不能用于：表名、列名、ORDER BY字段等SQL结构部分
     * ${} - 字符串替换：
     *      可以动态指定SQL结构：表名、列名、排序字段等
     *      有SQL注入风险：直接拼接，不做任何转义
     *      无法缓存执行计划：每次都是新的SQL语句
     *      需要手动校验：开发者必须确保参数安全
     * @param userId
     * @param tag
     * @param fieldName
     * @return
     */
    @Update("update t_user_tag set ${fieldName} = ${fieldName} | #{tag} where user_id=#{userId}")
    int setTag(Long userId, long tag, String fieldName);

    /**
     * 取消标签
     * @param userId
     * @param tag
     * @param fieldName
     * @return
     */
    @Update("update t_user_tag set ${fieldName} = ${fieldName} & ~ ${tag} where user_id= #{userId}")
    int cancelTag(Long userId, long tag, String fieldName);
}
