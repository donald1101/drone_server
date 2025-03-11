package com.hkxx.drone.common;

public class Crc16 {

	// 返回crc16校验，包含原始的数据
	public static byte[] getCrcData(byte[] data) {
		byte[] a = new byte[data.length - 2];
		byte[] b = new byte[data.length + 2];
		int j = 0;
		for (int i = 0; i < data.length; i++) {
			if (i > 1 && j < data.length - 2) {
				a[j] = data[i];
				j++;
			}
			b[i] = data[i];
		}
		int i;
		int crc = 0xFFFF;
		int i_data;
		for (i = 0; i < a.length; i++) {
			crc = (crc >> 8) | (crc << 8);
			crc &= 0xFFFF;
			i_data = a[i] & 0xFF;
			crc ^= i_data;
			crc ^= (crc & 0xFF) >> 4;
			crc ^= crc << 12;
			crc &= 0xFFFF;
			crc ^= (crc & 0xFF) << 5;
			crc &= 0xFFFF;
		}
		byte[] c = Convert.intToBytes(crc);
		b[b.length - 2] = c[3];
		b[b.length - 1] = c[2];
		return b;
	}

	/**
	 * CRC-CCITT(XModem) CRC-CCITT(0xFFFF) CRC-CCITT(0x1D0F) 校验模式
	 * 
	 * @param flag
	 *            < XModem(flag=1) 0xFFFF(flag=2) 0x1D0F(flag=3)>
	 * @param str
	 * @return
	 */
	public static short CRC_CCITT(int flag, byte[] bytes) {
		int crc = 0; // initial value
		int polynomial = 0x1021;
		// byte[] bytes = str.getBytes();

		switch (flag) {
		case 1:
			crc = 0x00;
			break;
		case 2:
			crc = 0xFFFF;
			break;
		case 3:
			crc = 0x1D0F;
			break;

		}
		for (int index = 0; index < bytes.length; index++) {
			byte b = bytes[index];
			for (int i = 0; i < 8; i++) {
				boolean bit = ((b >> (7 - i) & 1) == 1);
				boolean c15 = ((crc >> 15 & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
					crc ^= polynomial;
			}
		}
		crc &= 0xffff;

		return (short) crc;

	}

}
