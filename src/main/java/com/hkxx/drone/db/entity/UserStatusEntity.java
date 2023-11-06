package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author JiangCheng
 * @email jctc008@126.com
 * @date 2021-04-25 14:28:24
 */
@Data
@TableName("user_status")
public class UserStatusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增长主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 飞手id
     */
    private Integer userId;
    /**
     * 飞手姓名
     */
    private String userName;
    /**
     * 纬度
     */
    private Double lat;
    /**
     * 经度
     */
    private Double lng;
    /**
     * 任务ID
     */
    private Integer taskId;
    /**
     * 对应指令id
     */
    private Integer instructionId;
    /**
     * 最后更新时间
     */
    private Date updateTime;
    /**
     * 数据创建时间
     */
    private Date createdTime;

}
