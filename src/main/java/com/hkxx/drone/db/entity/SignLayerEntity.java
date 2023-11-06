package com.hkxx.drone.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 标记信息表
 * 
 * @author JiangCheng
 * @email jctc008@126.com
 * @date 2021-06-24 17:22:53
 */
@Data
@TableName("sign_layer")
public class SignLayerEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Integer id;
	/**
	 * 任务id
	 */
	private Integer taskId;
	/**
	 * 飞行ID
	 */
	private Integer flightLogId;
	/**
	 * 指令id
	 */
	private Integer instructionId;
	/**
	 * 标记名称
	 */
	private String signName;
	/**
	 * 标记类型 0.嫌疑人 1.嫌疑车 2.集合点 3.正射图像标记点 4.360全景图标记点 5.实景三维模型标记点 6.人工标记点
	 */
	private Integer signType;
	/**
	 * 标记几何类型 1 点 2 线 3 面
	 */
	private Integer geoType;
	/**
	 * 标记点的坐标信息
	 */
	private String position;
	/**
	 * 描述信息
	 */
	private String description;
	/**
	 * 记录人id 打下该标记的用户
	 */
	private String recordUserId;
	/**
	 * 图片 视频等
	 */
	private String file;
	/**
	 * 标记状态
	 */
	private Integer state;
	/**
	 * 创建时间
	 */
	private Date createdTime;
	/**
	 * 数据更新时间
	 */
	private Date updateTime;

}
