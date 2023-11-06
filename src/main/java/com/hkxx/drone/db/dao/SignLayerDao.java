package com.hkxx.drone.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.SignLayerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 标记信息表
 *
 * @author JiangCheng
 * @email jctc008@126.com
 * @date 2021-06-24 17:22:53
 */
@Mapper
public interface SignLayerDao extends BaseMapper<SignLayerEntity> {
    public SignLayerEntity selectByName(@Param("name") String name);
}
