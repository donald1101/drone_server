package com.hkxx.drone.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkxx.drone.db.entity.UserStatusEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JiangCheng
 * @email jctc008@126.com
 * @date 2021-04-25 14:28:24
 */
@Mapper
public interface UserStatusDao extends BaseMapper<UserStatusEntity> {
    public UserStatusEntity selectByUserId(@Param("user_id") int userId);
}
