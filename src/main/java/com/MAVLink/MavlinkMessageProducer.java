package com.MAVLink;

import com.MAVLink.common.msg_command_long;
import com.hkxx.common.Convert;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MavlinkMessageProducer {

	/**
	 *
	 * @param str
	 * @return
	 */
	public byte[] produceOrder(String str, int seq) throws Exception {

		if (str == null && "".equals(str)) {
			throw new Exception("命令不能为空");
		}
		msg_command_long msgCommandLong = new msg_command_long();

		msgCommandLong.command = 203;
		msgCommandLong.param1 = 0;
		msgCommandLong.param2 = 0;
		msgCommandLong.param3 = 0;
		msgCommandLong.param4 = 0;

		switch (str) {
		case Constants.TRIGGER_PHOTO:
			msgCommandLong.param5 = 1;
			break;
		case Constants.BACK_TO_MIDDLE:
			msgCommandLong.param5 = 5;
			break;
		case Constants.SWITCH_TYPE:
			msgCommandLong.param5 = 6;
			break;
		case Constants.TRIGGER_ViDEO:
			msgCommandLong.param4 = 1;
			msgCommandLong.param5 = 18;
			break;
		case Constants.IRC_NIGHT:
			msgCommandLong.param5 = 2;
			break;
		case Constants.IRC_DAY:
			msgCommandLong.param5 = 3;
			break;
		case Constants.IRC_AUTO:
			msgCommandLong.param5 = 4;
			break;
		case Constants.ZOOM_ADD:
			msgCommandLong.param4 = 1;
			msgCommandLong.param5 = 7;
			break;
		case Constants.ZOOM_SUB:
			msgCommandLong.param4 = -1;
			msgCommandLong.param5 = 7;
			break;
		case Constants.FOCUS_ADD:
			msgCommandLong.param4 = 1;
			msgCommandLong.param5 = 8;
			break;
		case Constants.FOCUS_SUB:
			msgCommandLong.param4 = -1;
			msgCommandLong.param5 = 8;
			break;
		case Constants.AUTO_FOCUS:
			msgCommandLong.param5 = 14;
			break;
		case Constants.LOCK_HEAD:
			msgCommandLong.param5 = 15;
			break;
		case Constants.FOLLOW:
			msgCommandLong.param5 = 16;
			break;
		case Constants.FOLLOWING:
			msgCommandLong.param5 = 13;
			break;
		case Constants.ROLLING:
			msgCommandLong.command = 205;
			msgCommandLong.param1 = 2;
			msgCommandLong.param2 = 0;
			msgCommandLong.param3 = 5;
			msgCommandLong.param4 = 0;
			msgCommandLong.param5 = 1;
			msgCommandLong.param6 = 0;
			msgCommandLong.param7 = 0;
			break;
		default:
			break;
		}

		byte[] bytes = msgCommandLong.pack().encodePacket();
		return bytes;

	}

	public static void main(String[] args) throws Exception {
		MavlinkMessageProducer mavlinkMessageProducer = new MavlinkMessageProducer();
		Field[] fields = Constants.class.getDeclaredFields();
		List<String> data = new ArrayList<String>();
		for (Field field : fields) {
			String cmdName = field.get(field.getName()).toString();
			byte[] bytes = mavlinkMessageProducer.produceOrder(cmdName, 0);
			String string = Convert.bytesToHexString(bytes);// 16进制的命令字符串
			System.out.println(cmdName + ": " + string);
			data.add(string);
		}
	}

}
