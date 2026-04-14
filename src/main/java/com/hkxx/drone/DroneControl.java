package com.hkxx.drone;

import com.hkxx.drone.common.DateTime;
import com.hkxx.drone.common.TcpClient;
import com.hkxx.drone.db.entity.PointFlyContent;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 无人机控制对象，包括mesh数传设备IP、端口号、控制targetID等关键信息
 */
public class DroneControl {

    private static Logger log = LoggerFactory.getLogger(DroneControl.class);

    private String devIP = "";    //数传IP地址
    private int devPort = 0;      //数传端口号
    private int targetID = 1;     //mavlink协议中的sys_id，用于唯一标识网络中的设备
    private int deviceId = 0; //该无人机在系统中的设备ID

    private Date lastSessionTime = DateTime.Now();
    private long sessionTimeout = 5000; //会话超时时间，若超过该时间，则认为设备掉线，需要进行重连，单位毫秒
    private TcpClient client = null;    //连接linkhub服务的客户端对象
    private int tsCheckState = 5000; //检测连接状态时间间隔
    private Thread tCheckConnection = null;
    private boolean isStopCheck = false;
    private Object synObject = new Object(); //信号量
    private int tsWait = 6000; //信号量等待时间，单位毫秒
    private int deviceState = 0; //无人机当前运行状态，3为standby已准备完毕，4为active飞行中
    private int deviceType = DeviceType.DRONE; //设备类型，默认为无人机

    public DroneControl() {
//        try {
//            lastSessionTime = DateTime.parse("1970-1-1 00:00:00", "yyyy-MM-dd HH:mm:ss");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public String getDevIP() {
        return devIP;
    }

    public void setDevIP(String devIP) {
        this.devIP = devIP;
    }

    public int getDevPort() {
        return devPort;
    }

    public void setDevPort(int devPort) {
        this.devPort = devPort;
    }

    public int getTargetID() {
        return targetID;
    }

    public void setTargetID(int targetID) {
        this.targetID = targetID;
    }

    public Date getLastSessionTime() {
        return lastSessionTime;
    }

    public void setLastSessionTime(Date lastSessionTime) {
        this.lastSessionTime = lastSessionTime;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(int deviceState) {
        this.deviceState = deviceState;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isEqual(DroneControl droneControl) {
        boolean rt = true;
        try {
            if (!this.devIP.equals(droneControl.getDevIP())) {
                rt = false;
                return rt;
            }
            if (devPort != droneControl.getDevPort()) {
                rt = false;
                return rt;
            }
            if (deviceType != droneControl.getDeviceType()) {
                rt = false;
                return rt;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public void startConnection() {
        try {
            if (client == null) {
                client = new TcpClient();
                String serverIP = "";
                int serverPort = 0;
                serverIP = devIP;
                serverPort = devPort;
                client.setServerIP(serverIP);
                client.setIdleTime(Config.idleTime);
                client.setConnectTimeout(Config.connectTimeout);
                client.setServerPort(serverPort);
                MavlinkEncoder encoder = new MavlinkEncoder();
                MavlinkDecoder decoder = new MavlinkDecoder();
                client.setFilter(new ProtocolCodecFilter(new SimpleCodecFactory(
                        decoder, encoder)));
                MavlinkHandler handler = new MavlinkHandler();
                handler.setDroneControl(this);
                client.setHandler(handler);
                client.initial();
                client.connect();
            }
            //开启连接状态监测线程，实现掉线后自动重连
            isStopCheck = false;
            if (tCheckConnection == null) {
                tCheckConnection = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long tsSpan = 0; //当前时间与上次会话时间的差值，单位毫秒
                            long tsTimeout = sessionTimeout;
                            while (!isStopCheck) {
                                try {
                                    tsSpan = DateTime.Now().getTime() - lastSessionTime.getTime();
//                                    log.info("TimeSpan:"+tsSpan+"timeout:"+tsTimeout);
                                    if (tsSpan > tsTimeout) {
                                        //会话超时，需要重新连接
                                        log.info("Device connection is timeout.Trying to reconnect...");
                                        client.close();
                                        client.connect();
                                    }
                                    Thread.sleep(tsCheckState);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            synchronized (synObject) {
                                synObject.notifyAll();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                tCheckConnection.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() {
        try {
            synchronized (synObject) {
                isStopCheck = true;
                if (client != null) {
                    client.close();
                    client.dispose();
                    client = null;
                }
                synObject.wait(tsWait);
            }
            tCheckConnection = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //上传航点飞行任务
    public boolean uploadMission(PointFlyContent content) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.uploadMission(client.getSession(), content);
                //发送阵形
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //开始执行航点飞行任务
    public boolean startMission(int targetSystemId, int targetComponentId, int missionCount) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.startMission(client.getSession(), targetSystemId, targetComponentId, missionCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //停止执行航点飞行任务
    public boolean stopMission(int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.stopMission(client.getSession(), targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //暂停执行航点飞行任务
    public boolean pauseMission(int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.pauseMission(client.getSession(), targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //恢复执行航点飞行任务
    public boolean resumeMission(int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.resumeMission(client.getSession(), targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //返航
    public boolean goHome(int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.goHome(client.getSession(), targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }


}
