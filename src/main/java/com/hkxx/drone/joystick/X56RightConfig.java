package com.hkxx.drone.joystick;

import com.hkxx.common.DateTime;
import com.hkxx.common.TcpClient;
import com.hkxx.common.UdpClient;
import com.hkxx.drone.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * x56Left摇杆按键映射配置
 */
public class X56RightConfig implements CommandResultHandler.SessionTimeUpdatedListener {
    private boolean tcpMode = false;
    private boolean udpMode = false;
    private String serverIp = "127.0.0.1";
    private int tcpPort = 60000;
    private int udpPort = 60000;
    private boolean serialPortMode = false;
    private String serialPortNumber = "COM1";
    private String protocolType = ProtocolType.JSON_ADD_CRLF;
    private String x = "";
    private String y = "";
    private String rz = "";
    private String hatSwitch = "";
    private String rx = "";
    private String ry = "";
    private List<ButtonMap> buttonMaps = new ArrayList<>(); //17个button
    //发送给udp服务器的协议参数
    private int deviceId = 0;
    private int deviceType = 0;
    private int sysId = 0;

    //功能性操作
    private static Logger log = LoggerFactory.getLogger(X56LeftConfig.class);
    private TcpClient tcpClient = null;
    private UdpClient udpClient = null;
    private transient int tsCheckState = 5000; //检测连接状态时间间隔
    private Thread tCheckConnection = null;
    private transient boolean isStopCheck = false;
    private transient Object synObject = new Object(); //信号量
    private transient int tsWait = 6000; //信号量等待时间，单位毫秒
    private transient Date lastSessionTime = DateTime.Now();
    private transient long sessionTimeout = 5000; //会话超时时间，若超过该时间，则认为设备掉线，需要进行重连，单位毫秒

    public void startConnection() {
        try {
            if (tcpMode) {
                if (tcpClient == null) {
                    tcpClient = new TcpClient();
                    tcpClient.setServerIP(serverIp);
                    tcpClient.setIdleTime(Config.idleTime);
                    tcpClient.setConnectTimeout(Config.connectTimeout);
                    tcpClient.setServerPort(tcpPort);
                    switch (protocolType) {
                        case ProtocolType.JSON_ADD_CRLF:
                            CommandEncoder encoder = new CommandEncoder();
                            CommandDecoder decoder = new CommandDecoder();
                            tcpClient.setFilter(new ProtocolCodecFilter(new SimpleCodecFactory(
                                    decoder, encoder)));
                            CommandResultHandler handler = new CommandResultHandler();
                            handler.setSessionTimeUpdatedListener(this);
                            tcpClient.setHandler(handler);
                            tcpClient.initial();
                            tcpClient.connect();
                            break;
                        case ProtocolType.MAVLINK:
                            break;
                        default:
                            break;
                    }

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
                                            log.info("CommandService connection is timeout.Trying to reconnect...");
                                            tcpClient.close();
                                            tcpClient.connect();
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
            }
            if (udpMode) {
                if (udpClient == null) {
                    udpClient = new UdpClient();
                    udpClient.setServerIP(serverIp);
                    udpClient.setIdleTime(Config.idleTime);
                    udpClient.setConnectTimeout(Config.connectTimeout);
                    udpClient.setServerPort(udpPort);
                    switch (protocolType) {
                        case ProtocolType.JSON_ADD_CRLF:
                            CommandEncoder encoder = new CommandEncoder();
                            CommandDecoder decoder = new CommandDecoder();
                            udpClient.setFilter(new ProtocolCodecFilter(new SimpleCodecFactory(
                                    decoder, encoder)));
                            CommandResultHandler handler = new CommandResultHandler();
                            udpClient.setHandler(handler);
                            udpClient.initial();
                            udpClient.connect();
                            break;
                        case ProtocolType.MAVLINK:
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() {
        try {
            if (tcpMode) {
                synchronized (synObject) {
                    isStopCheck = true;
                    if (tcpClient != null) {
                        tcpClient.close();
                        tcpClient.dispose();
                        tcpClient = null;
                    }
                    synObject.wait(tsWait);
                }
                tCheckConnection = null;
            }
            if (udpMode) {
                if (udpClient != null) {
                    udpClient.close();
                    udpClient.dispose();
                    udpClient = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(Object data) {
        try {
            if (tcpMode) {
                if (tcpClient != null) {
                    tcpClient.send(data);
                }
            }
            if (udpMode) {
                if (udpClient != null) {
                    udpClient.send(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(String data) {
        sendData(IoBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)));
    }

    public void sendData(byte[] data) {
        sendData(IoBuffer.wrap(data));
    }

    public boolean isTcpMode() {
        return tcpMode;
    }

    public void setTcpMode(boolean tcpMode) {
        this.tcpMode = tcpMode;
    }

    public boolean isUdpMode() {
        return udpMode;
    }

    public void setUdpMode(boolean udpMode) {
        this.udpMode = udpMode;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public boolean isSerialPortMode() {
        return serialPortMode;
    }

    public void setSerialPortMode(boolean serialPortMode) {
        this.serialPortMode = serialPortMode;
    }

    public String getSerialPortNumber() {
        return serialPortNumber;
    }

    public void setSerialPortNumber(String serialPortNumber) {
        this.serialPortNumber = serialPortNumber;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getRz() {
        return rz;
    }

    public void setRz(String rz) {
        this.rz = rz;
    }

    public String getHatSwitch() {
        return hatSwitch;
    }

    public void setHatSwitch(String hatSwitch) {
        this.hatSwitch = hatSwitch;
    }

    public String getRx() {
        return rx;
    }

    public void setRx(String rx) {
        this.rx = rx;
    }

    public String getRy() {
        return ry;
    }

    public void setRy(String ry) {
        this.ry = ry;
    }

    public List<ButtonMap> getButtonMaps() {
        return buttonMaps;
    }

    public void setButtonMaps(List<ButtonMap> buttonMaps) {
        this.buttonMaps = buttonMaps;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getSysId() {
        return sysId;
    }

    public void setSysId(int sysId) {
        this.sysId = sysId;
    }

    @Override
    public void onSessionTimeUpdated() {
        lastSessionTime = DateTime.Now();
    }
}
