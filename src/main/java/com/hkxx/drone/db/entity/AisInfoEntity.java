package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ais信息表
 * 
 * @author JiangCheng
 * @email jctc008@126.com
 * @date 2021-06-24 17:22:53
 */
@Data
@TableName("ais_info")
public class AisInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId
	private Integer id;
	/**
	 * Ais类别 A类或B类
	 */
	private String channel;
	/**
	 * MMSI编号
	 */
	private String mmsiId;
	/**
	 * 导航状态
	 */
	private String navigationStatus;
	/**
	 * 地面航速
	 */
	private Double sog;
	/**
	 * 经度
	 */
	private Double longitude;
	/**
	 * 纬度
	 */
	private Double latitude;
	/**
	 * 地面航线
	 */
	private Double cog;
	/**
	 * 实际航向
	 */
	private Double course;
	/**
	 * 时戳
	 */
	private Integer epfsTime;
	/**
	 * IMO编号
	 */
	private String imo;
	/**
	 * 呼号
	 */
	private String callNum;
	/**
	 * 供应商ID
	 */
	private String supplierId;
	/**
	 * 船舶名称
	 */
	private String name;
	/**
	 * 船舶类型或货物类型
	 */
	private String type;
	/**
	 * 船舶尺寸、参考位置
	 */
	private String size;
	/**
	 * 电子定位类型
	 */
	private String gpsType;
	/**
	 * 估计到达时间
	 */
	private Date eta;
	/**
	 * 目前最大静态吃水
	 */
	private Double maxStaticDraught;
	/**
	 * 目的地
	 */
	private String destination;
	/**
	 * 最后通信时间
	 */
	private Date updateTime;
	/**
	 * 备注
	 */
	private String remark;

}
