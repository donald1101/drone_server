package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("port")
public class PortEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId
    private Integer id;
    /**
     *
     */
    private Integer value;
    /**
     *
     */
    private Integer category;
    /**
     *
     */
    @TableField("deviceId")
    private Integer deviceId;
    /**
     *
     */
    @TableField("replayId")
    private Integer replayId;
    /**
     *
     */
    private Integer available;

}
