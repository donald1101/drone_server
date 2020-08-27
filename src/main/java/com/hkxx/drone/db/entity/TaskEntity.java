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
@TableName("task")
public class TaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId
    private Integer id;
    /**
     *
     */
    private Integer planid;
    /**
     *
     */
    private String name;
    /**
     *
     */
    private Integer status;
    /**
     * 0：航点飞行 1：区域飞行 2：全景图飞行 3：集群飞行
     */
    private Integer taskType;
    /**
     *
     */
    private Integer rept;
    /**
     * 默认飞行高度
     */
    private Float height;
    /**
     *
     */
    private Date createddatetime;
    /**
     *
     */
    private Date nextrun;
    /**
     *
     */
    private Integer expectedoperationtime;
    /**
     *
     */
    private Date updateDatetime;
    /**
     *
     */
    private Double distance;

}
