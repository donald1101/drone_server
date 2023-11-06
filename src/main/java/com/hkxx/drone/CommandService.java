package com.hkxx.drone;

import com.hkxx.common.TcpServer;
import com.hkxx.common.UdpServer;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CommandService {

    private static Logger log = LoggerFactory.getLogger(CommandService.class);

    Thread tCheckCommandState = null;
    boolean isStop = false;
    int tsSendHeartbeat = 5000; //发送心跳包的间隔，单位毫秒，默认5秒
    private TcpServer tcpServer = null;
    private UdpServer udpServer = null;
    boolean isTcpServerSendHeartbeat = true;
    boolean isUdpServerSendHeartbeat = true;

    public void start() {
        tCheckCommandState = new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("CommandService is started.");
                isStop = false;
                byte[] heartbeat = "{}\r\n".getBytes(StandardCharsets.UTF_8);
                while (!isStop) {
                    try {
                        if (isTcpServerSendHeartbeat) {
                            tcpServerBroadcastData(heartbeat);
                        }
                        if (isUdpServerSendHeartbeat) {
                            udpServerBroadcastData(heartbeat);
                        }
                        Thread.sleep(tsSendHeartbeat);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                log.info("CommandService is stopped.");
            }
        });
        tCheckCommandState.start();
    }

    public void stop() {
        isStop = true;
    }

    public void tcpServerBroadcastData(byte[] data) {
        try {
            if (tcpServer != null) {
                Map<Long, IoSession> sessionMap = tcpServer.getAcceptor().getManagedSessions();
                for (IoSession session : sessionMap.values()) {
                    session.write(IoBuffer.wrap(data));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void udpServerBroadcastData(byte[] data) {
        try {
            if (udpServer != null) {
                Map<Long, IoSession> sessionMap = udpServer.getAcceptor().getManagedSessions();
                for (IoSession session : sessionMap.values()) {
                    session.write(IoBuffer.wrap(data));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getTsSendHeartbeat() {
        return tsSendHeartbeat;
    }

    public void setTsSendHeartbeat(int tsSendHeartbeat) {
        this.tsSendHeartbeat = tsSendHeartbeat;
    }

    public TcpServer getTcpServer() {
        return tcpServer;
    }

    public void setTcpServer(TcpServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    public UdpServer getUdpServer() {
        return udpServer;
    }

    public void setUdpServer(UdpServer udpServer) {
        this.udpServer = udpServer;
    }

    public boolean isTcpServerSendHeartbeat() {
        return isTcpServerSendHeartbeat;
    }

    public void setTcpServerSendHeartbeat(boolean tcpServerSendHeartbeat) {
        isTcpServerSendHeartbeat = tcpServerSendHeartbeat;
    }

    public boolean isUdpServerSendHeartbeat() {
        return isUdpServerSendHeartbeat;
    }

    public void setUdpServerSendHeartbeat(boolean udpServerSendHeartbeat) {
        isUdpServerSendHeartbeat = udpServerSendHeartbeat;
    }
}
