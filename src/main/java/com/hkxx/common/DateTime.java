package com.hkxx.drone.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTime {

	public static Date Now() {
		return new Date();
	}

	public static String getNowTime() {
		String rt = "";
		try {
			Date now = new Date();
			SimpleDateFormat dtFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			rt = dtFormat.format(now);
		} catch (Exception e) {
			// TODO: handle exception
			rt = "";
		}
		return rt;
	}

	public static String getNowTime(String format) {
		String rt = "";
		try {
			Date now = new Date();
			SimpleDateFormat dtFormat = new SimpleDateFormat(format);
			rt = dtFormat.format(now);
		} catch (Exception e) {
			// TODO: handle exception
			rt = "";
		}
		return rt;
	}

	public static String getTimeString(Date dt) {
		String rt = "";
		try {
			SimpleDateFormat dtFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			rt = dtFormat.format(dt);
		} catch (Exception e) {
			// TODO: handle exception
			rt = "";
		}
		return rt;
	}

	public static String getTimeString(Date dt, String format) {
		String rt = "";
		try {
			SimpleDateFormat dtFormat = new SimpleDateFormat(format);
			rt = dtFormat.format(dt);
		} catch (Exception e) {
			// TODO: handle exception
			rt = "";
		}
		return rt;
	}

	public static Date parse(String time) {
		Date rt = null;
		try {
			SimpleDateFormat dtFormat = new SimpleDateFormat();
			rt = dtFormat.parse(time);
		} catch (Exception e) {
			// TODO: handle exception
			rt = null;
		}
		return rt;
	}

	public static Date parse(String time, String format) {
		Date rt = null;
		try {
			SimpleDateFormat dtFormat = new SimpleDateFormat(format);
			rt = dtFormat.parse(time);
		} catch (Exception e) {
			// TODO: handle exception
			rt = null;
		}
		return rt;
	}

	// 在一个date上面增加time，返回一个新date，单位毫秒
	public static String addTime(Date dt, long time) {
		String rt = "";
		try {
			Date newDate = new Date(dt.getTime() + time);
			rt = DateTime.getTimeString(newDate);
		} catch (Exception e) {
			// TODO: handle exception
			rt = "";
		}
		return rt;

	}

	// 将UTC的秒数转换为当前时间
	public static Date fromUTCSecondToTime(long seconds) {
		Date rt = null;
		try {
			rt = new Date(seconds * 1000);
		} catch (Exception e) {
			// TODO: handle exception
			rt = null;
		}
		return rt;
	}
}
