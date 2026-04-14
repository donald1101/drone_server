package com.hkxx.drone.db.entity;

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
public class DroneEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 设备id
     */
    private Integer deviceId;
    /**
     * 无人机名称
     */
    private String name;

    private int deviceType;

    private String deviceIp;

    private int devicePort;


    public DroneEntity() {

    }
}
