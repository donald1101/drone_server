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
@TableName("flightpath")
public class FlightpathEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId
    private Integer id;
    /**
     *
     */
    private Integer executorid;
    /**
     *
     */
    private Integer executortype;
    /**
     *
     */
    private Integer planid;
    /**
     *
     */
    private Integer taskid;
    /**
     *
     */
    private Integer collaboration;
    /**
     *
     */
    private Float height;
    /**
     *
     */
    private Integer finishaction;
    /**
     *
     */
    private Float speed;
    /**
     *
     */
    private Float distance;
    /**
     *
     */
    private String content;

}
