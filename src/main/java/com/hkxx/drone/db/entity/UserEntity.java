package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author JiangCheng
 * @email sunlightcs@gmail.com
 * @date 2020-08-27 10:36:31
 */
@Data
@TableName("user")
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId
    private Integer id;
    /**
     *
     */
    private Integer permission;
    /**
     *
     */
    private String username;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private String email;
    /**
     *
     */
    private String comment;
    /**
     *
     */
    @TableField(value = "created_time")
    private Date createTime;

}
