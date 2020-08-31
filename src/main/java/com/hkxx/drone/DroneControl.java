package com.hkxx.drone;

import com.hkxx.common.DateTime;
import com.hkxx.common.TcpClient;
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
    private String linkhubIP = "127.0.0.1";       //linkhub服务的IP地址
    private int linkhubPort = 58000;              //linkhub服务开放无人机控制的端口号
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
    private int controlType = ControlType.CONTROL_CLUSTER; //控制类型

    private DeviceStateChanged deviceStateChangedListener = null;

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

    public String getLinkhubIP() {
        return linkhubIP;
    }

    public void setLinkhubIP(String linkhubIP) {
        this.linkhubIP = linkhubIP;
    }

    public int getLinkhubPort() {
        return linkhubPort;
    }

    public void setLinkhubPort(int linkhubPort) {
        this.linkhubPort = linkhubPort;
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

    public int getControlType() {
        return controlType;
    }

    public void setControlType(int controlType) {
        this.controlType = controlType;
    }

    public DeviceStateChanged getDeviceStateChangedListener() {
        return deviceStateChangedListener;
    }

    public void setDeviceStateChangedListener(DeviceStateChanged deviceStateChangedListener) {
        this.deviceStateChangedListener = deviceStateChangedListener;
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
            if (!this.linkhubIP.equals(droneControl.getLinkhubIP())) {
                rt = false;
                return rt;
            }
            if (linkhubPort != droneControl.getLinkhubPort()) {
                rt = false;
                return rt;
            }
            if (deviceType != droneControl.getDeviceType()) {
                rt = false;
                return rt;
            }
            if (controlType != droneControl.getControlType()) {
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
                switch (controlType) {
                    case ControlType.CONTROL_CLUSTER:
                        serverIP = linkhubIP;
                        serverPort = linkhubPort;
                        break;
                    case ControlType.CONTROL_DIRECT:
                        serverIP = devIP;
                        serverPort = devPort;
                        break;
                    default:
                        break;
                }
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

    //发送阵形
    public boolean sendCollaboration(int targetSystemId, int targetComponentId, int collaboration) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.sendCollaboration(client.getSession(), targetSystemId, targetComponentId, collaboration);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //发送阵形编排值
    public boolean setCollaborationValue(int targetSystemId, int targetComponentId, int collaborationValue) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.setCollaborationValue(client.getSession(), targetSystemId, targetComponentId, collaborationValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //发送hold指令
    public boolean setHold(int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.setHold(client.getSession(), targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //返航
    public boolean gotoPosition(int targetSystemId, int targetComponentId, double lat, double lng, float alt) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.gotoPosition(client.getSession(), targetSystemId, targetComponentId, lat, lng, alt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //发送起飞指令，起飞到指定高度
    public boolean takeoff(int targetSystemId, int targetComponentId, double lat, double lng, float alt) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.takeoff(client.getSession(), targetSystemId, targetComponentId, lat, lng, alt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //发送起飞指令，起飞到指定高度，按照当前经纬度
    public boolean takeoff(int targetSystemId, int targetComponentId, float alt) {
        boolean rt = false;
        try {
            if (client != null) {
                MavlinkHandler handler = (MavlinkHandler) client.getHandler();
                rt = handler.takeoff(client.getSession(), targetSystemId, targetComponentId, alt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public void notifyDeviceStateChanged(int state) {
        try {
            if (deviceStateChangedListener != null) {
                deviceStateChangedListener.onDeviceStateChanged(deviceId, state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface DeviceStateChanged {
        public void onDeviceStateChanged(int deviceId, int state);
    }
}
