package com.hkxx.mq;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.hkxx.drone.CommandType;
import com.hkxx.drone.Config;
import com.hkxx.drone.DroneService;
import com.hkxx.drone.MavlinkHandler;
import com.hkxx.drone.db.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RabbitServer {
    private static Logger log = LoggerFactory.getLogger(RabbitServer.class);
    private DroneService droneService = null;
    private RabbitConsumer rabbitConsumer = null;
    private RabbitProducer rabbitProducer = null;
    boolean isStop = false;
    Thread thread = null;

    public RabbitProducer getRabbitProducer() {
        return rabbitProducer;
    }

    public void setRabbitProducer(RabbitProducer rabbitProducer) {
        this.rabbitProducer = rabbitProducer;
    }

    public RabbitConsumer getRabbitConsumer() {
        return rabbitConsumer;
    }

    public void setRabbitConsumer(RabbitConsumer rabbitConsumer) {
        this.rabbitConsumer = rabbitConsumer;
    }

    public DroneService getDroneService() {
        return droneService;
    }

    public void setDroneService(DroneService droneService) {
        this.droneService = droneService;
    }

    public void start() {
       try{
           log.info("RabbitServer is started.");
           rabbitProducer.init();
           rabbitConsumer.init();
           thread = new Thread(new Runnable() {
               @Override
               public void run() {
                   while(!isStop) {
                       String message = rabbitConsumer.getMessage();
                       try {
//                           messageDecoder(message);
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   }
               }
           });
           thread.start();
       }catch (Exception e){
           e.printStackTrace();
       }

    }
    public void stop() {
        isStop = true;
    }

    public void messageDecoder(String msg) throws Exception {
        CommandResult commandResult = new CommandResult();
        if(msg.startsWith("cmd")) {
            Command command = JSON.parseObject(msg, Command.class);
            if (command != null) {
                switch (command.getCmd()) {
                    case CommandType.UPLOAD_TASK:
                        uploadTask(command);
                        break;
                    case CommandType.SEND_CMD:
                        sendCmd(command);
                        break;
                    default:
                        commandResult.setCode(500);
                        commandResult.setResult("[*] The command format is incorrect.");
                        log.info("[*] The command format is incorrect.");
                        break;
                }
            }else{
                commandResult.setCode(500);
                commandResult.setResult("[*] The command format is incorrect.");
                log.info("[*] The command format is incorrect.");
            }
            rabbitProducer.messageSend(JSON.toJSONString(commandResult));
        }else{
            commandResult = JSON.parseObject(msg, CommandResult.class);
            rabbitProducer.messageSend(JSON.toJSONString(commandResult));
            System.out.println("[*] CommandResult : { "+commandResult.getCode()+ " : "+commandResult.getResult() +" }");
        }
    }

    public void uploadTask(Command command){
        CommandResult commandResult = new CommandResult();
        if (droneService != null) {
            boolean rt = false;
            PointFlyContent pointFlyContent = JSON.toJavaObject((JSON) command.getParam(), PointFlyContent.class);
            for(DroneEntity drone:Config.droneList) {
                if( drone.getName().equals( pointFlyContent.getEntityName() ) ){
                    int deviceID = drone.getDeviceId();
                    rt = droneService.uploadMission(deviceID,pointFlyContent);
                }
            }
            if (rt) {
                commandResult.setCode(200);
                commandResult.setResult("Upload task is success.");
            } else {
                commandResult.setCode(500);
                commandResult.setResult("Upload task is failed.");
            }
            try {
                rabbitProducer.messageSend(JSON.toJSONString(commandResult));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            commandResult.setCode(500);
            commandResult.setResult("Drone service is not alive.");
            try {
                rabbitProducer.messageSend(JSON.toJSONString(commandResult));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCmd(Command command){

    }

}