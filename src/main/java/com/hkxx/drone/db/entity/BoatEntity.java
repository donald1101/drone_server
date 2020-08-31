package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author JiangCheng
 * @email sunlightcs@gmail.com
 * @date 2020-08-27 10:36:31
 */
@Data
@TableName("boat")
public class BoatEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId
	private Integer id;
	/**
	 * 设备id
	 */
	private Integer deviceId;
	/**
	 * 无人机名称
	 */
	private String name;
	/**
	 * 型号
	 */
	private String model;
	/**
	 * 品牌
	 */
	private String brand;
	/**
	 * 序列号
	 */
	private String serial;
	/**
	 * 购买时间
	 */
	private Date purchasetime;
	/**
	 * 负责人
	 */
	private String manager;
	/**
	 * 设备状态
	 */
	private Integer serstate;
	/**
	 * 最大飞行高度
	 */
	private Float maxheight;
	/**
	 * 最大飞行航程
	 */
	private Float maxlength;
	/**
	 * 转弯半径
	 */
	private Float turnradius;
	/**
	 * 任务id
	 */
	private Integer taskid;
	/**
	 * 飞行id
	 */
	private Integer flightid;
	/**
	 * 
	 */
	private Integer targetid;
	/**
	 * 优先控制权
	 */
	private Integer permission;
	/**
	 * 图片地址
	 */
	private String photourl;
	/**
	 * 
	 */
	private String wsaddress;
	/**
	 * 
	 */
	private String liveaddress;
	/**
	 * 
	 */
	private String linkaddress;
	/**
	 * 
	 */
	private String wsprefix;
	/**
	 * 最大俯仰角
	 */
	private Float maxel;
	/**
	 * 最低飞行高度
	 */
	private Float minflyheight;
	/**
	 * 最低飞行速度
	 */
	private Float minflyspeed;
	/**
	 * 起飞高度
	 */
	private Float takeoffheight;
	/**
	 * 返航高度
	 */
	private Float returnheight;
	/**
	 * 返航悬停高度
	 */
	private Float returnhoverheight;
	/**
	 * 最大巡航速度
	 */
	private Float maxcruisespeed;
	/**
	 * 微控速度
	 */
	private Float maxmicrocontrolspeed;
	/**
	 * 最大上升高度
	 */
	private Float maxrisespeed;
	/**
	 * 最大下降高度
	 */
	private Float maxdownspeed;
	/**
	 * 失联返航状态
	 */
	private Integer lossofcontactreturnstatus;
	/**
	 * 无人机标牌设置
	 */
	private Integer uavsignsetting;
	/**
	 * 无人机控制类型 0：集群控制 1：移动控制
	 */
	private Integer controlType;
	/**
	 * 设备类型
	 */
	private Integer dronetype;

}
