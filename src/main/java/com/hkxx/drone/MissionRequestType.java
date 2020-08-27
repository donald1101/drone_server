package com.hkxx.drone;

/**
 * 描述上传mission时，发送请求的类型，用来兼容mavlink中的mission_request消息
 * 老版本的协议中mission_request消息被mission_request_int消息代替
 */
public class MissionRequestType {
    public static int MISSION_REQUEST = 0;
    public static int MISSION_REQUEST_INT = 1;
}
