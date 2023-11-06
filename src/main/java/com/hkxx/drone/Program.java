package com.hkxx.drone;

import com.hkxx.common.TcpServer;
import com.hkxx.common.UdpServer;
import com.hkxx.drone.joystick.JoystickService;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.util.Properties;

public class Program implements KeyListener {

    private static Logger log = LoggerFactory.getLogger(Program.class);

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {

            try {
//                Scanner s = new Scanner(System.in);
//                System.out.println("请输入字符串：");
//                while (true) {
//                    String line = s.nextLine();
//                    if (line.equals("exit")) break;
//                    System.out.println(">>>" + line);
//                }

                System.out.println(System.getProperty("user.dir"));
                Properties prop = new Properties();
                InputStream config = Program.class
                        .getResourceAsStream("/set.properties");
                prop.load(config);
                Config.isUseDroneService = Boolean.parseBoolean(prop.getProperty("isUseDroneService"));
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

                Config.isUseJoystickService = Boolean.parseBoolean(prop.getProperty("isUseJoystickService"));
                Config.joystickConfigFile = prop.getProperty("joystickConfigFile");
            } catch (Exception e) {
                // TODO: handle exception
                log.error(e.getMessage());
            }

            //开启无人机控制服务
            DroneService droneService = null;
            if (Config.isUseDroneService) {
                droneService = new DroneService();
                droneService.setTsCheckTime(Config.checkDroneTime);
                droneService.start();
            }
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
                handler.setCommandService(commandService);
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
                handler.setCommandService(commandService);
                udpServer.setHandler(handler);
                udpServer.start();
                commandService.setUdpServer(udpServer);
            }
            commandService.start();

            JoystickService joystickService = null;
            if (Config.isUseJoystickService) {
                //开启摇杆接入服务
                joystickService = new JoystickService();
                joystickService.start();
            }

        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {
        log.info("keyTyped:" + e.getKeyCode());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        log.info("keyTyped:" + e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        log.info("keyTyped:" + e.getKeyCode());
    }
}
