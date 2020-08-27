package com.hkxx.drone.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.SwarmDevicePort;
import com.hkxx.drone.db.entity.SwarmEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-18 19:04:49
 */
@Mapper
public interface SwarmDao extends BaseMapper<SwarmEntity> {

    public List<SwarmDevicePort> selectSwarmDevicePorts(@Param("swarmId") int swarmId);
}
