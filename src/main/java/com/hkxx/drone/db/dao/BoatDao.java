package com.hkxx.drone.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.BoatEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JiangCheng
 * @email sunlightcs@gmail.com
 * @date 2020-08-27 10:36:31
 */
@Mapper
public interface BoatDao extends BaseMapper<BoatEntity> {
    public List<BoatEntity> selectByControlType(@Param("controlType") int controlType);

    public BoatEntity selectByDeviceId(@Param("deviceId") int deviceId);

    public BoatEntity selectBySysId(@Param("sysId") int sysId);

    /**
     * @param controlTypes 传入多个controlType进行查找，格式为(1,2,3 ...)
     * @return
     */
    public List<BoatEntity> selectByControlTypes(@Param("controlTypes") int[] controlTypes);
}
