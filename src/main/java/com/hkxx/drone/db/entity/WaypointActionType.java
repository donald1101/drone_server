package com.hkxx.drone.db.entity;

public class WaypointActionType {

    public static final int STAY = 0;
    public static final int START_TAKE_PHOTO = 1;
    public static final int START_RECORD = 2;
    public static final int STOP_RECORD = 3;
    public static final int ROTATE_AIRCRAFT = 4;
    public static final int GIMBAL_PITCH = 5;

    public static String getActionTypeName(int typeId) {
        String rt = "";
        try {
            switch (typeId) {
                case STAY:
                    rt = "悬停";
                    break;
                case START_TAKE_PHOTO:
                    rt = "拍照";
                    break;
                case START_RECORD:
                    rt = "开始录像";
                    break;
                case STOP_RECORD:
                    rt = "停止录像";
                    break;
                case ROTATE_AIRCRAFT:
                    rt = "飞行器偏航角";
                    break;
                case GIMBAL_PITCH:
                    rt = "云台俯仰角";
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = "";
        }
        return rt;
    }
}
