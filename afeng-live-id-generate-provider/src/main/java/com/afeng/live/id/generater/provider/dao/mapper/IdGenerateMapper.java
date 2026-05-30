package com.afeng.live.id.generater.provider.dao.mapper;

import com.afeng.live.id.generater.provider.dao.po.IdGeneratePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface IdGenerateMapper extends BaseMapper<IdGeneratePO> {

    /**
     * 查询所有
     * @return
     */
    @Select("select * from t_id_generate_config")
    List<IdGeneratePO> selectAll();

    @Update("update t_id_generate_config set next_threshold=next_threshold+step,"+
            "current_start=current_start+step,version=version+1 where id = #{id} and version=#{version}")
    int updateNewIdCountAndVersion(@Param("id") Long id,@Param("version") Long version);
}
