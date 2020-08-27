package com.hkxx.common;

import java.io.*;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Convert {

    private static double EARTH_RADIUS = 6378.137;// 单位千米，地球半径

    /*
     * java二进制,字节数组,字符,十六进制,BCD编码转换2007-06-07 00:17 * /
     *
     *
     * /** 把16进制字符串转换成字节数组
     *
     * @param hex
     *
     * @return
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toUpperCase().toCharArray(); // 必须全部为大写，才能正确获取索引，若为小写则可能出错
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /** */
    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray
     * @return
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    // 字节数组转十六进制字符串，是否添加空格
    public static final String bytesToHexString(byte[] bArray, boolean addSpace) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
            if (addSpace) {
                // 添加空格隔开
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /** */
    /**
     * 把字节数组转换为对象
     *
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static final Object bytesToObject(byte[] bytes) throws IOException,
            ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream oi = new ObjectInputStream(in);
        Object o = oi.readObject();
        oi.close();
        return o;
    }

    /** */
    /**
     * 把可序列化对象转换成字节数组
     *
     * @param s
     * @return
     * @throws IOException
     */
    public static final byte[] objectToBytes(Serializable s) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream ot = new ObjectOutputStream(out);
        ot.writeObject(s);
        ot.flush();
        ot.close();
        return out.toByteArray();
    }

    public static final String objectToHexString(Serializable s)
            throws IOException {
        return bytesToHexString(objectToBytes(s));
    }

    public static final Object hexStringToObject(String hex)
            throws IOException, ClassNotFoundException {
        return bytesToObject(hexStringToByte(hex));
    }

    /** */
    /**
     * @函数功能: BCD码转为10进制串(阿拉伯数据)
     * @输入参数: BCD码
     * @输出结果: 10进制串
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }

    /** */
    /**
     * @函数功能: 10进制串转为BCD码
     * @输入参数: 10进制串
     * @输出结果: BCD码
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;

        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }

        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }

        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;

        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * 把int转化为byte数组
     *
     * @param value 传入用户id
     * @see [类、类#方法、类#成员]
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static int byte2int(byte[] res) {
        // 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000
        int targets = (res[3] & 0xff) | ((res[2] << 8) & 0xff00) // | 表示安位或
                | ((res[1] << 24) >>> 8) | (res[0] << 24);
        return targets;
    }

    public static byte[] shortToByte(short number) {
        int value = number;
        // byte[] b = new byte[2];
        // for (int i = 0; i < b.length; i++) {
        // b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
        // temp = temp >> 8; // 向右移8位
        // }
        byte[] b = new byte[2];
        b[0] = (byte) ((value >> 8) & 0xFF);
        b[1] = (byte) (value & 0xFF);
        return b;
    }

    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[1] & 0xff);// 最低位
        short s1 = (short) (b[0] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * 截取byte数组
     *
     * @param count 截取长度
     * @param buff  截取数组
     * @param start 开始
     * @param end   结束
     */
    public static byte[] cutByteArray(int count, byte[] buff, int start, int end) {
        byte[] nTemp = new byte[count];
        try {
            int flag = 0;
            for (int i = start; i <= end; i++) {
                nTemp[flag] = buff[i];
                flag++;
            }
        } catch (Exception e) {
            // TODO: handle exception

        }
        return nTemp;
    }

    /** */
    /**
     * MD5加密字符串，返回加密后的16进制字符串
     *
     * @param origin
     * @return
     */
    public static String MD5EncodeToHex(String origin) {
        return bytesToHexString(MD5Encode(origin));
    }

    /** */
    /**
     * MD5加密字符串，返回加密后的字节数组
     *
     * @param origin
     * @return
     */
    public static byte[] MD5Encode(String origin) {
        return MD5Encode(origin.getBytes());
    }

    /** */
    /**
     * MD5加密字节数组，返回加密后的字节数组
     *
     * @param bytes
     * @return
     */
    public static byte[] MD5Encode(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // 计算xor校验
    public static byte getXor(byte[] buf, int start, int end) {
        int xor = 0;
        try {
            for (int i = start; i < end; i++) {
                xor ^= (buf[i] & 0xff);
            }
        } catch (Exception e) {
            // TODO: handle exception
            xor = 0;
        }
        return (byte) (xor & 0xff);
    }

    // 计算和校验，不考虑多余1字节的部分，不考虑符号位
    public static byte getPlus(byte[] buf, int start, int end) {
        int xor = 0;
        try {
            for (int i = start; i < end; i++) {
                xor += (buf[i] & 0xff);
            }
        } catch (Exception e) {
            // TODO: handle exception
            xor = 0;
        }
        return (byte) (xor & 0xff);
    }

    // 判断一个字符串是否为IP地址格式
    public static boolean ipCheck(String text) {
        if (text != null && !text.equals("")) {
            // 定义正则表达式
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            // 判断ip地址是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        return false;
    }

    // 获取字符串数组指定项的索引
    public static int getStringArrayIndex(String[] buf, String item) {
        int rt = -1;
        try {
            for (int i = 0; i < buf.length; i++) {
                if (buf[i].equals(item)) {
                    rt = i;
                    break;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            rt = -1;
        }
        return rt;
    }

    /**
     * 角度弧度计算公式 rad:(). <br/>
     * <p>
     * 360度=2π π=Math.PI
     * <p>
     * x度 = x*π/360 弧度
     *
     * @param d
     * @return
     * @author chiwei
     * @since JDK 1.6
     */
    public static double getRadian(double degree) {
        return degree * Math.PI / 180.0;
    }

    /**
     * 根据经纬度计算两点之间的距离 GetDistance:(). <br/>
     *
     * @param lat1 1点的纬度
     * @param lng1 1点的经度
     * @param lat2 2点的纬度
     * @param lng2 2点的经度
     * @return 距离 单位 米
     * @author chiwei
     * @since JDK 1.6
     */
    public static double getDistanceByGPS(double lat1, double lng1,
                                          double lat2, double lng2) {
        double radLat1 = getRadian(lat1);
        double radLat2 = getRadian(lat2);
        double a = radLat1 - radLat2;// 两点纬度差
        double b = getRadian(lng1) - getRadian(lng2);// 两点的经度差
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s * 1000;
    }

    public static String getHexString(byte data) {
        StringBuffer sb = new StringBuffer();
        String sTemp = Integer.toHexString(0xFF & data);
        if (sTemp.length() < 2)
            sb.append(0);
        sb.append(sTemp.toUpperCase());
        return sb.toString();
    }

    public static Long bitStringToLong(String bits) {
        Long rt = Long.valueOf(0);
        try {
            // bits必须为“01”组成的字符串
            long temp = 0;
            long move = 1;
            char c;
            for (int i = bits.length() - 1; i >= 0; i--) {
                c = bits.charAt(i);
                if (c == '1') {
                    temp += move << ((bits.length() - 1) - i);
                } else if (c == '0') {

                } else {
                    rt = null;
                    return rt;
                }
                // temp = temp << 1;
            }
            rt = Long.valueOf(temp);

        } catch (Exception e) {
            // TODO: handle exception
            rt = null;
        }
        return rt;
    }

    /**
     * 字节转换为浮点
     *
     * @param b 字节（至少4个字节）
     * @param b 开始位置
     * @return
     */
    public static float getFloat(byte[] b) {
        int accum = 0;
        accum = accum | (b[0] & 0xff) << 0;
        accum = accum | (b[1] & 0xff) << 8;
        accum = accum | (b[2] & 0xff) << 16;
        accum = accum | (b[3] & 0xff) << 24;
        System.out.println(accum);
        return Float.intBitsToFloat(accum);
    }

    /**
     * 四元数转欧拉角
     *
     * @param quat  四元数数组
     * @param angle 反解出的三个角度
     * @return
     */
    public static double[] quaternion_2_euler(double quat[], double angle[]) {
        double tag = quat[0] * quat[2] - quat[1] * quat[3];
        double epsilon = 0.0009765625f;
        double threshold = 0.5f - epsilon;

        // 奇异姿态,俯仰角为 ±90 度
        if (tag < -threshold || tag > threshold) {
            int sign = sign(tag);
            angle[0] = -2 * sign * (double) Math.atan2(quat[1], quat[0]); // yaw
            angle[1] = sign * (double) (Math.PI / 2.0); // pitch
            angle[2] = 0; // roll
        } else {
            double a = 2.0f * (quat[3] * quat[2] + quat[0] * quat[1]);
            double b = 1.0f - 2.0f * (quat[1] * quat[1] + quat[2] * quat[2]);
            angle[0] = Math.atan2(a, b);

            double c = 2.0f * (quat[2] * quat[0] - quat[3] * quat[1]);
            angle[1] = Math.asin(c);

            double d = 2.0f * (quat[3] * quat[0] + quat[1] * quat[2]);
            double e = 1.0f - 2.0f * (quat[2] * quat[2] + quat[3] * quat[3]);
            angle[2] = Math.atan2(d, e);
        }
        double scale = (float) (180.0f / Math.PI);
        //弧度转换角度
        angle[0] = angle[0] * scale;//roll
        angle[1] = angle[1] * scale;//pitch
        angle[2] = angle[2] * scale;//yaw
        return angle;
    }

    static int sign(double param) {
        if (param > 0) {
            return 1;
        } else if (param == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * 两个数的平方和开根号
     *
     * @return
     */
    public static Double sqrt(float num1, float num2) {
        return Math.sqrt(Math.pow(num1, 2) + Math.pow(num2, 2));
    }

    /**
     * 转换double字符串，保留两位小数
     * double字符串
     */
    public static double convertDouble(String doubleNumberStr) {
        BigDecimal db = new BigDecimal(doubleNumberStr);
        //当数组过小时可能会出现科学计数法 例如 12345E-10
        //使用BigDecimal类进行处理
        String formatNumber = db.toPlainString();
        //保留两位小数
        String str = String.format("%.2f", Double.parseDouble(formatNumber));
        return Double.parseDouble(str);
    }

    /**
     * 获取一个字节的八个bit位,并以数组进行返回。
     * 低位在前,高位在后。
     *
     * @param a
     * @return
     */
    public static byte[] byteToBits(byte a) {
        byte[] temp = new byte[8];
        for (int i = 7; i >= 0; i--) {
            temp[i] = (byte) ((a >> i) & 1);
        }
        return temp;
    }

}
