package com.hkxx.drone;

import com.hkxx.drone.common.TcpServer;
import com.hkxx.drone.common.UdpServer;
import com.hkxx.drone.db.entity.DroneEntity;
import com.hkxx.mq.RabbitConsumer;
import com.hkxx.mq.RabbitProducer;
import com.hkxx.mq.RabbitServer;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Program{

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
//                Config.deviceUseRTCP = Boolean.parseBoolean(prop.getProperty("deviceUseRTCP"));
                Config.connectTimeout = Integer.parseInt(prop.getProperty(("connectTimeout")));
                Config.idleTime = Integer.parseInt(prop.getProperty("idleTime"));
                Config.sessionTimeout = Long.parseLong(prop.getProperty("sessionTimeout")) * 1000;
                Config.checkDroneTime = Integer.parseInt(prop.getProperty("checkDroneTime")) * 1000;
                Config.gcsSystemId = Integer.parseInt(prop.getProperty(("gcsSystemId")));
//                Config.cmdTcpMode = Boolean.parseBoolean(prop.getProperty("cmdTcpMode"));
//                Config.cmdTcpPort = Integer.parseInt(prop.getProperty("cmdTcpPort"));
//                Config.cmdUdpMode = Boolean.parseBoolean(prop.getProperty("cmdUdpMode"));
//                Config.cmdUdpPort = Integer.parseInt(prop.getProperty("cmdUdpPort"));
//                Config.tsSendHeartbeat = Integer.parseInt(prop.getProperty("tsSendHeartbeat"));
//                Config.isTcpServerSendHeartbeat = Boolean.parseBoolean(prop.getProperty("isTcpServerSendHeartbeat"));
//                Config.isUdpServerSendHeartbeat = Boolean.parseBoolean(prop.getProperty("isUdpServerSendHeartbeat"));
                Config.isUseRabbitService = Boolean.parseBoolean(prop.getProperty("isUseRabbitService"));
                Config.FROM_EXCHANGE_NAME = prop.getProperty("FROM_EXCHANGE_NAME");
                Config.TO_EXCHANGE_NAME = prop.getProperty("TO_EXCHANGE_NAME");
                //初始化无人机实体
                Config.drone1 = new DroneEntity();
                Config.drone1.setDeviceId(Integer.parseInt(prop.getProperty("deviceId1")));
                Config.drone1.setName(prop.getProperty("deviceName1"));
                Config.drone1.setDeviceType(Integer.parseInt(prop.getProperty(("deviceType1"))));
                Config.drone1.setDeviceIp(prop.getProperty("deviceIp1"));
                Config.drone1.setDevicePort(Integer.parseInt(prop.getProperty(("devicePort1"))));

                Config.droneList = new ArrayList<>(Arrays.asList(Config.drone1));
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

            RabbitServer rabbitServer= null;
            if (Config.isUseRabbitService) {
                RabbitProducer rabbitProducer = new RabbitProducer();
                RabbitConsumer rabbitConsumer = new RabbitConsumer();
                rabbitServer = new RabbitServer();
                rabbitServer.setRabbitProducer(rabbitProducer);
                rabbitServer.setRabbitConsumer(rabbitConsumer);
                rabbitServer.setDroneService(droneService);
                rabbitServer.start();
            }


        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }

    }
}
