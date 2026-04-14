package com.hkxx.drone;

import com.hkxx.drone.db.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * 无人机控制服务类，提供无人机的连接、控制等功能
 */
public class DroneService {

    private static Logger log = LoggerFactory.getLogger(DroneService.class);

    Thread tCheckDroneState = null;
    boolean isStop = false;
    int tsCheckTime = 30 * 1000; //检测无人机控制对象的周期，单位毫秒，默认30秒
    //无人机控制对象哈希表，无人机的序号ID为key
    private HashMap<Integer, DroneControl> droneMap = new HashMap<>();
    private Object synObject = new Object();
    // Ports value - start from
    int from = 58000;
    // Ports value - to
    int to = 58100;

    public int getTsCheckTime() {
        return tsCheckTime;
    }

    public void setTsCheckTime(int tsCheckTime) {
        this.tsCheckTime = tsCheckTime;
    }

    public void start() {
        try {
            //扫描数据库，判断drone表是否有变化，若有变化则更新无人机控制表droneMap，更新linkhub配置文件通知linkhub服务刷新
            isStop = false;
            tCheckDroneState = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("DroneService is started.");

                    while (!isStop) {

                        try {
                            List<DroneEntity> droneList = Config.droneList;
                            DroneEntity drone = null;
                            DroneControl droneControl = null;
                            //加载新的无人机连接对象
                            for (int i = 0; i < droneList.size(); i++) {
                                drone = droneList.get(i);
                                droneControl = new DroneControl();
                                droneControl.setDevIP(drone.getDeviceIp());
                                droneControl.setDevPort(drone.getDevicePort());
                                droneControl.setSessionTimeout(Config.sessionTimeout);
                                droneControl.setDeviceId(drone.getDeviceId()); //保存设备ID
                                droneControl.setDeviceType(DeviceType.DRONE);
                                if (droneMap.containsKey(drone.getDeviceId())) {
                                    droneControl.startConnection();//开启连接
                                } else {
                                    //若不存在，则添加无人机控制对象至droneMap，建立连接
                                    droneMap.put(drone.getDeviceId(), droneControl);
                                    droneControl.startConnection();//开启连接
                                }
                            }

                            //清理资源，释放内存，提醒虚拟机回收内存
                            System.gc();
                            //强制调用已经失去引用对象的finalize方法
                            System.runFinalization();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(tsCheckTime);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    log.info("DroneService is stopped.");
                }
            });
            tCheckDroneState.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isStop = true;
    }


    //上传航点飞行任务到指定的无人机
    public boolean uploadMission(int deviceId, PointFlyContent content) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.uploadMission(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //给指定的无人机，发送开始起飞指令，执行航点飞行任务
    public boolean startMission(int deviceId, int targetSystemId, int targetComponentId, int missionCount) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.startMission(targetSystemId, targetComponentId, missionCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //停止执行航点飞行任务
    public boolean stopMission(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.stopMission(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //暂停执行航点飞行任务
    public boolean pauseMission(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.pauseMission(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //恢复执行航点飞行任务
    public boolean resumeMission(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.resumeMission(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //返航
    public boolean goHome(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.goHome(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

}
