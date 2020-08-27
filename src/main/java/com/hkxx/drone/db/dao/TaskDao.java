package com.hkxx.drone.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.TaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-07-25 11:53:36
 */
@Mapper
public interface TaskDao extends BaseMapper<TaskEntity> {

}
