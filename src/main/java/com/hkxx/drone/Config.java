package com.hkxx.drone;

import com.hkxx.drone.db.entity.DroneEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
    //无人机控制服务配置参数
    public static boolean isUseDroneService = true; //是否启用无人机控制服务，如果做外网级联，有可能该服务无需启用
    public static boolean deviceUseRTCP = true;
    public static int connectTimeout = 30; // 连接超时时间，单位秒
    public static int idleTime = 10 * 60; // Session进入空闲状态的时间间隔，单位秒，管理端共用
    public static long sessionTimeout = 5000; //会话超时时间，若超过该时间，则认为设备掉线，需要进行重连，单位毫秒
    public static int checkDroneTime = 30 * 1000; //检测无人机控制对象的周期，单位毫秒，默认30秒
    public static int gcsSystemId = 255; //地面控制端符合mavlink协议的systemId，默认255

    //管理控制端配置参数
    public static boolean cmdTcpMode = true;
    public static int cmdTcpPort = 60000;
    public static boolean cmdUdpMode = true;
    public static int cmdUdpPort = 60000;
    public static int tsSendHeartbeat = 5000; //发送心跳包的间隔，单位毫秒，默认5秒
    public static boolean isTcpServerSendHeartbeat = true;
    public static boolean isUdpServerSendHeartbeat = true;

    //消息队列配置参数
    public static String Host = "127.0.0.1";
    public static int    Port = 5672;
    public static String Username = "guest";
    public static String Password = "guest";
    public static String VirtualHost = "/";
    public static String TO_EXCHANGE_NAME;
    public static String FROM_EXCHANGE_NAME;

    //无人机实体管理
    public static DroneEntity drone1;
//    public static DroneEntity drone2;
    public static List<DroneEntity> droneList;

    public static boolean isUseRabbitService = true;

}
