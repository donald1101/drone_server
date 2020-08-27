package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-18 19:04:49
 */
@Data
@TableName("swarm_device")
public class SwarmDeviceEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId
	private Integer id;
	/**
	 * 集群id
	 */
	private Integer swarmId;
	/**
	 * 设备id
	 */
	private Integer deviceId;
	/**
	 * 是否为长机
	 */
	private Integer isHead;

}
