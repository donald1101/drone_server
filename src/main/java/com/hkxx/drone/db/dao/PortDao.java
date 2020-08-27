package com.hkxx.drone.db.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.PortEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-07-25 11:53:36
 */
@Mapper
public interface PortDao extends BaseMapper<PortEntity> {

    public List<PortEntity> selectByDeviceId(@Param("deviceId") Integer deviceId, @Param("available") Integer available, @Param("category") Integer category, @Param("offset") int offset, @Param("limit") int limit);
}
