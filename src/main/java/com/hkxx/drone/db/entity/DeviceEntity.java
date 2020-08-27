package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-07-25 11:53:36
 */
@Data
@TableName("device")
public class DeviceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备类型
     */
    private Integer deviceType;
    /**
     * 设备编号
     */
    private String deviceNo;
    /**
     * 备注
     */
    private String remark;

}
