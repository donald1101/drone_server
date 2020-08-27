package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-07-25 11:53:36
 */
@Data
@TableName("current_status")
public class CurrentStatusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     *
     */
    private Integer droneId;
    /**
     * 设备id
     */
    private Integer deviceId;
    /**
     * 相对高度，单位米
     */
    private Float alt;
    /**
     * 纬度
     */
    private Double lat;
    /**
     * 经度
     */
    private Double lng;
    /**
     * 速度，指水平速度
     */
    private Float horizontalSpeed;
    /**
     * 垂直速度
     */
    private Float verticalSpeed;
    /**
     * 偏航角
     */
    private Float yaw;
    /**
     * 翻滚角
     */
    private Float roll;
    /**
     * 俯仰角
     */
    private Float pitch;
    /**
     * 电池电量，百分比
     */
    private Float battery;
    /**
     * 相机云台偏航角
     */
    private Float gimbalYaw;
    /**
     * 相机云台翻滚角
     */
    private Float gimbalRoll;
    /**
     * 相机云台俯仰角
     */
    private Float gimbalPitch;
    /**
     * 时间戳
     */
    private Date updateTime;
    /**
     *
     */
    private Float hdg;
    /**
     * 0：未知 1：启动中 2：校准中 3：已准备完毕 4：飞行中 5：异常 6：紧急状态 7：关机中 8：结束飞行中
     */
    private Integer state;
}
