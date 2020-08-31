package com.hkxx.drone;

import com.hkxx.common.TcpServer;
import com.hkxx.common.UdpServer;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class Program {

    private static Logger log = LoggerFactory.getLogger(Program.class);

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {

            try {
                System.out.println(System.getProperty("user.dir"));
                Properties prop = new Properties();
                InputStream config = Program.class
                        .getResourceAsStream("/set.properties");
                prop.load(config);

                Config.deviceUseRTCP = Boolean.parseBoolean(prop.getProperty("deviceUseRTCP"));
                Config.linkhubIP = prop.getProperty("linkhubIP");
                Config.linkhubProfilePath = prop.getProperty("linkhubProfilePath");
                Config.mavlogWorkspace = prop.getProperty("mavlogWorkspace");
                Config.connectTimeout = Integer.parseInt(prop.getProperty(("connectTimeout")));
                Config.idleTime = Integer.parseInt(prop.getProperty("idleTime"));
                Config.sessionTimeout = Long.parseLong(prop.getProperty("sessionTimeout")) * 1000;
                Config.checkDroneTime = Integer.parseInt(prop.getProperty("checkDroneTime")) * 1000;
                Config.gcsSystemId = Integer.parseInt(prop.getProperty(("gcsSystemId")));

                Config.cmdTcpMode = Boolean.parseBoolean(prop.getProperty("cmdTcpMode"));
                Config.cmdTcpPort = Integer.parseInt(prop.getProperty("cmdTcpPort"));
                Config.cmdUdpMode = Boolean.parseBoolean(prop.getProperty("cmdUdpMode"));
                Config.cmdUdpPort = Integer.parseInt(prop.getProperty("cmdUdpPort"));
                Config.tsSendHeartbeat = Integer.parseInt(prop.getProperty("tsSendHeartbeat"));
                Config.isTcpServerSendHeartbeat = Boolean.parseBoolean(prop.getProperty("isTcpServerSendHeartbeat"));
                Config.isUdpServerSendHeartbeat = Boolean.parseBoolean(prop.getProperty("isUdpServerSendHeartbeat"));

            } catch (Exception e) {
                // TODO: handle exception
                log.error(e.getMessage());
            }

            //开启无人机控制服务
            DroneService droneService = new DroneService();
            droneService.setTsCheckTime(Config.checkDroneTime);
            droneService.start();
            CommandService commandService = new CommandService();
            commandService.setTsSendHeartbeat(Config.tsSendHeartbeat);
            commandService.setTcpServerSendHeartbeat(Config.isTcpServerSendHeartbeat);
            commandService.setUdpServerSendHeartbeat(Config.isUdpServerSendHeartbeat);
            if (Config.cmdTcpMode) {
                //开启管理端tcp服务器
                TcpServer tcpServer = new TcpServer();
                tcpServer.setIdleTime(Config.idleTime);
                tcpServer.setServerPort(Config.cmdTcpPort);
                CommandEncoder encoder = new CommandEncoder();
                CommandDecoder decoder = new CommandDecoder();
                tcpServer.setFilter(new ProtocolCodecFilter(
                        new SimpleCodecFactory(decoder, encoder)));
                CommandHandler handler = new CommandHandler();
                handler.setDroneService(droneService);
                tcpServer.setHandler(handler);
                tcpServer.start();
                commandService.setTcpServer(tcpServer);
            }

            if (Config.cmdUdpMode) {
                //开启管理端udp服务器
                UdpServer udpServer = new UdpServer();
                udpServer.setIdleTime(Config.idleTime);
                udpServer.setServerPort(Config.cmdUdpPort);
                CommandEncoder encoder = new CommandEncoder();
                CommandDecoder decoder = new CommandDecoder();
                udpServer.setFilter(new ProtocolCodecFilter(
                        new SimpleCodecFactory(decoder, encoder)));
                CommandHandler handler = new CommandHandler();
                handler.setDroneService(droneService);
                udpServer.setHandler(handler);
                udpServer.start();
                commandService.setUdpServer(udpServer);
            }
            commandService.start();

        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }

    }

}
