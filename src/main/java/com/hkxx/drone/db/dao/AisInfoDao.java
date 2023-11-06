package com.hkxx.drone.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.AisInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ais信息表
 *
 * @author JiangCheng
 * @email jctc008@126.com
 * @date 2021-06-24 17:22:53
 */
@Mapper
public interface AisInfoDao extends BaseMapper<AisInfoEntity> {
    public AisInfoEntity selectByMMSI(@Param("mmsi") String mmsi);
}
