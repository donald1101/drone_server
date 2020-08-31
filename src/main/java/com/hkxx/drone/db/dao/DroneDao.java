package com.hkxx.drone.db.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.DroneEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-07-25 11:53:36
 */
@Mapper
public interface DroneDao extends BaseMapper<DroneEntity> {

    public List<DroneEntity> selectByControlType(@Param("controlType") int controlType);

    public DroneEntity selectByDeviceId(@Param("deviceId") int deviceId);

    public DroneEntity selectBySysId(@Param("sysId") int sysId);

    /**
     * @param controlTypes 传入多个controlType进行查找，格式为(1,2,3 ...)
     * @return
     */
    public List<DroneEntity> selectByControlTypes(@Param("controlTypes") int[] controlTypes);
}
