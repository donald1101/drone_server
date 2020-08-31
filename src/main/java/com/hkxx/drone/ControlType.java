package com.hkxx.drone;

public class ControlType {
    /**
     * 集群控制，采用DMate控制，通过linkhub进行中转控制
     */
    public static final int CONTROL_CLUSTER = 0;

    /**
     * 移动控制，采用移动端控制，下发到移动端APP进行控制
     */
    public static final int CONTROL_MOBILE = 1;

    /**
     * 直连控制，不通过linkhub，直接连接设备进行控制
     */
    public static final int CONTROL_DIRECT = 2;
}
