package com.hkxx.drone.db.dao;

import com.hkxx.drone.db.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author JiangCheng
 * @email sunlightcs@gmail.com
 * @date 2020-08-27 10:36:31
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
	
}
