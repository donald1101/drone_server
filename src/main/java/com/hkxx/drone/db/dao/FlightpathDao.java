package com.hkxx.drone.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.FlightpathEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-07-25 11:53:36
 */
@Mapper
public interface FlightpathDao extends BaseMapper<FlightpathEntity> {
    public List<FlightpathEntity> selectByTaskId(@Param("taskId") int taskId);
}
