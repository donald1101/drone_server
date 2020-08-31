package com.hkxx.drone;

import com.MAVLink.common.*;
import com.MAVLink.enums.MAV_COMPONENT;
import com.hkxx.common.Convert;
import com.hkxx.common.DateTime;
import com.hkxx.drone.db.MybatisUtil;
import com.hkxx.drone.db.dao.CurrentStatusDao;
import com.hkxx.drone.db.entity.CurrentStatusEntity;
import com.hkxx.drone.db.entity.PointFlyContent;
import com.hkxx.drone.db.entity.Waypoint;
import io.dronefleet.mavlink.common.*;
import io.dronefleet.mavlink.protocol.MavlinkPacket;
import io.dronefleet.mavlink.util.EnumValue;
import org.apache.ibatis.session.SqlSession;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MavlinkHandler extends IoHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(MavlinkHandler.class);

    DroneControl droneControl = null; //无人机控制对象引用
    int mavlink_version = 1; //mavlink协议版本，1或者2，根据接收到的心跳包确定协议版本
    int seq = 0; // 顺序号
    Object synObject = new Object(); // 互斥信号量
    int tsWait = 5000; // 等待信号量事件通知的时间，单位毫秒
    boolean isOk = false;

    //mission任务相关变量
    Object synMissionObject = new Object(); // 互斥信号量，防止同时上传多个mission
    int msgTimeout = 1500; //发送msg的默认超时时间，单位毫秒，默认1500毫秒
    int missionItemTimeout = 2500; //发送或获取missionItem的默认超时间隔，单位毫秒，默认250毫秒
    int retries = 5; //尝试重复发送消息的次数，默认5次
    int requestSeq = 0; //无人机请求mission_item的序号
    MavMissionResult missionResult = MavMissionResult.MAV_MISSION_INVALID;
    Object synCmdObject = new Object(); // 互斥信号量，防止同时执行多个command
    MavResult cmdResult = MavResult.MAV_RESULT_FAILED; //命令执行的结果
    int missionRequestType = MissionRequestType.MISSION_REQUEST_INT; //mission请求的类型，用来兼容老版本的上传mission协议，老版本的协议中mission_request消息被mission_request_int消息代替
    double currentLat = 0; //无人机当前的纬度
    double currentLng = 0; //无人机当前的经度
    float currentAlt = 0; //无人机当前高度，相对home点的高度，不是海拔高度
//    String paramSetId = ""; //设置参数名字
//    float paramSetValue = 0; //设置参数

    //param参数列表
    HashMap<String, Float> paramList = new HashMap<>();

    //统计接收到的msgId类型，存放到列表中，用于调试分析
    List<Integer> msgIds = new ArrayList<>();

    public DroneControl getDroneControl() {
        return droneControl;
    }

    public void setDroneControl(DroneControl droneControl) {
        this.droneControl = droneControl;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        // TODO Auto-generated method stub
        super.exceptionCaught(session, cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        // TODO Auto-generated method stub
        // super.messageReceived(session, message);
        try {
            IoBuffer recv = (IoBuffer) message;
            // 将IBuffer转换为字节数组
            byte[] data = new byte[recv.remaining()];
            recv.get(data, 0, data.length);
            String msg = "";

            // 用16进制的格式打印出来显示
            msg = Convert.bytesToHexString(data, true);
            // log.info("Recv:" + msg);


            int stx = (data[0] & 0xff);
            MavlinkPacket mavlinkPacket = null;
            if (stx == 0xfe) {
                // mavlink第一版协议
                mavlink_version = 1;
                mavlinkPacket = MavlinkPacket.fromV1Bytes(data);
            } else if (stx == 0xfd) {
                // mavlink第二版协议
                mavlink_version = 2;
                mavlinkPacket = MavlinkPacket.fromV2Bytes(data);
            }
            if (!msgIds.contains(mavlinkPacket.getMessageId())) {
                msgIds.add(mavlinkPacket.getMessageId());
                log.info("New type of mavlink msg is received.Msg ID:" + mavlinkPacket.getMessageId() + " Version:" + mavlink_version);
                //输出所有的msg id类型
                log.info("MsgId:" + msgIds.toString());
            }
            // 根据msgId，具体处理对应的协议指令
            switch (mavlinkPacket.getMessageId()) {
                case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                    processMsgHeartbeat(session, mavlinkPacket);
                    break;
                case msg_command_int.MAVLINK_MSG_ID_COMMAND_INT:
                    break;
                case msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG:
                    processMsgCommandLong(session, mavlinkPacket);
                    break;
                case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
                    processMsgCommandAck(session, mavlinkPacket);
                    break;
                case msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST:
                    processMsgMissionRequest(session, mavlinkPacket);
                    break;
                case msg_mission_request_int
                        .MAVLINK_MSG_ID_MISSION_REQUEST_INT:
                    processMsgMissionRequestInt(session, mavlinkPacket);
                    break;
                case msg_mission_ack
                        .MAVLINK_MSG_ID_MISSION_ACK:
                    processMsgMissionAck(session, mavlinkPacket);
                    break;
                case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
                    processMsgAttitude(session, mavlinkPacket);
                    break;
                case msg_attitude_quaternion.MAVLINK_MSG_ID_ATTITUDE_QUATERNION:
                    processMsgAttitudeQuaternion(session, mavlinkPacket);
                    break;
                case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
                    proMsgGlobalPosition(session, mavlinkPacket);
                    break;
                case 180:
                    proMsgVmountAttitude(session, mavlinkPacket);
                    break;
                case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
                    proMsgSysStatus(session, mavlinkPacket);
                    break;
                case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
                    proMsgGpsRawInt(session, mavlinkPacket);
                    break;
                case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
                    proMsgParamValue(session, mavlinkPacket);
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }

    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        // TODO Auto-generated method stub
        super.messageSent(session, message);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        super.sessionClosed(session);
        InetSocketAddress remote = (InetSocketAddress) session
                .getRemoteAddress();
        log.info("Session closed." + remote.getHostString() + ":"
                + remote.getPort());
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        super.sessionCreated(session);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        // TODO Auto-generated method stub
        super.sessionIdle(session, status);
        // InetSocketAddress remote = (InetSocketAddress) session
        // .getRemoteAddress();
        // log.info("IdleStatus changed.Status:" + status.toString() + " "
        // + remote.getHostString() + "：" + remote.getPort());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        // super.sessionOpened(session);
        InetSocketAddress remote = (InetSocketAddress) session
                .getRemoteAddress();
        log.info("Session opened." + remote.getHostString() + ":"
                + remote.getPort());
    }

    private void processMsgHeartbeat(IoSession session, MavlinkPacket packet) {
        // log.info("recv Heartbeat:" + Convert.bytesToHexString(data, true));
        try {
            Heartbeat heartbeat = (Heartbeat) MavlinkUtil.packetToPayloadObject(packet);
            CurrentStatusEntity msgDroneStatus = new CurrentStatusEntity();
            msgDroneStatus.setDeviceId(droneControl.getDeviceId());
            msgDroneStatus.setUpdateTime(DateTime.Now());
            msgDroneStatus.setState(heartbeat.systemStatus().value());
            updateOrInsertCurrentStatus(msgDroneStatus);

            //更新会话时间戳
            if (droneControl != null) {
                droneControl.setLastSessionTime(new Date());
                //检测无人机状态跳变
                int lastState = droneControl.getDeviceState();
                droneControl.setDeviceState(msgDroneStatus.getState());
                int nowState = droneControl.getDeviceState();
                if (lastState == 3 && nowState == 4) {
                    //表示从standby到active，说明无人机起飞了，传递state为0,
                    droneControl.notifyDeviceStateChanged(0);
                } else if (lastState == 4 && nowState == 3) {
                    //表示从active到standby，说明无人机已经着陆，任务结束
                    droneControl.notifyDeviceStateChanged(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMsgCommandLong(IoSession session, MavlinkPacket packet) {
        //log.info("recv CommandLong:" + Convert.bytesToHexString(data, true));
    }

    private void processMsgCommandAck(IoSession session, MavlinkPacket packet) {
        try {
            CommandAck commandAck = (CommandAck) MavlinkUtil.packetToPayloadObject(packet);
            cmdResult = commandAck.result().entry();
            synchronized (synObject) {
                isOk = true;
                synObject.notify();
            }
            log.info("Recv CommandAck.Result: " + commandAck.result().entry().name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMsgMissionRequest(IoSession session, MavlinkPacket packet) {
        try {
            MissionRequest missionRequest = (MissionRequest) MavlinkUtil.packetToPayloadObject(packet);
            requestSeq = missionRequest.seq();
            missionRequestType = MissionRequestType.MISSION_REQUEST;
            synchronized (synObject) {
                isOk = true;
                synObject.notify();
            }
            log.info("Recv MissionRequest.Seq: " + requestSeq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processMsgMissionRequestInt(IoSession session, MavlinkPacket packet) {
        try {
            MissionRequestInt missionRequestInt = (MissionRequestInt) MavlinkUtil.packetToPayloadObject(packet);
            requestSeq = missionRequestInt.seq();
            missionRequestType = MissionRequestType.MISSION_REQUEST_INT;
            synchronized (synObject) {
                isOk = true;
                synObject.notify();
            }
            log.info("Recv MissionRequestInt.Seq: " + requestSeq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMsgMissionAck(IoSession session, MavlinkPacket packet) {
        try {
            MissionAck missionAck = (MissionAck) MavlinkUtil.packetToPayloadObject(packet);
            missionResult = missionAck.type().entry();
            synchronized (synObject) {
                isOk = true;
                synObject.notify();
            }
            log.info("Recv MissionAck.Result: " + missionAck.type().entry().name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMsgAttitude(IoSession session, MavlinkPacket packet) {
        try {
            //log.info("recv Attitude:" + Convert.bytesToHexString(packet.getRawBytes(), true));
            Attitude attitude = (Attitude) MavlinkUtil.packetToPayloadObject(packet);

            CurrentStatusEntity msgDroneStatus = new CurrentStatusEntity();
            msgDroneStatus.setDeviceId(droneControl.getDeviceId());
            msgDroneStatus.setUpdateTime(DateTime.Now());

            msgDroneStatus.setPitch(Float.parseFloat(String.format("%.2f", attitude.pitch() * 180 / Math.PI)));
            msgDroneStatus.setRoll(Float.parseFloat(String.format("%.2f", attitude.roll() * 180 / Math.PI)));
            //将yaw转换为[0,360]度之间，原始数据为[-pi,pi]的弧度值
            double yawDegree = 0;
            float yaw = attitude.yaw();
            if (yaw >= 0 && yaw <= Math.PI) {
                yawDegree = (yaw * 180) / Math.PI;
            } else {
                yawDegree = ((yaw + 2 * Math.PI) * 180) / Math.PI;
            }
            msgDroneStatus.setYaw(Float.parseFloat(String.format("%.2f", yawDegree)));
            updateOrInsertCurrentStatus(msgDroneStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processMsgAttitudeQuaternion(IoSession session, MavlinkPacket packet) {
        try {
            //log.info("recv AttitudeQuaternion:" + Convert.bytesToHexString(packet.getRawBytes(), true));
            AttitudeQuaternion attitudeQuaternion = (AttitudeQuaternion) MavlinkUtil.packetToPayloadObject(packet);
            double q1 = attitudeQuaternion.q1();
            double q2 = attitudeQuaternion.q2();
            double q3 = attitudeQuaternion.q3();
            double q4 = attitudeQuaternion.q4();
            double quat[] = {q1, q2, q3, q4};
            double[] doubles = Convert.quaternion_2_euler(quat, new double[3]);
            double roll = doubles[0];
            double pitch = doubles[1];
            double yaw = doubles[2];

            double q11 = Convert.convertDouble(String.valueOf(q1));
            double q22 = Convert.convertDouble(String.valueOf(q2));
            double q33 = Convert.convertDouble(String.valueOf(q3));
            double q44 = Convert.convertDouble(String.valueOf(q4));
            String formatStr = "q1:" + q11 + " q2:" + q22 + " q3:" + q33 + " q4:" + q44;
            //writeDataToFile(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + formatStr, "msgAttitudeQuaternion.txt");
            //log.info(formatStr);

            CurrentStatusEntity msgDroneStatus = new CurrentStatusEntity();
            msgDroneStatus.setDeviceId(droneControl.getDeviceId());
            msgDroneStatus.setUpdateTime(DateTime.Now());

            msgDroneStatus.setPitch((float) Convert.convertDouble(String.valueOf(pitch)));
            msgDroneStatus.setRoll((float) Convert.convertDouble(String.valueOf(roll)));
            msgDroneStatus.setYaw((float) Convert.convertDouble(String.valueOf(yaw)));
            updateOrInsertCurrentStatus(msgDroneStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proMsgGlobalPosition(IoSession session, MavlinkPacket packet) {
        try {
            //log.info("recv GlobalPosition:" + Convert.bytesToHexString(packet.getRawBytes(), true));
            GlobalPositionInt globalPositionInt = (GlobalPositionInt) MavlinkUtil.packetToPayloadObject(packet);

            //将值进行打印
            //writeDataToFile(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss")+" "+globalPositionIntMavlinkMessage.toString(),"msgGlobalPosition.txt");

            //组装数据库的操作bean
            CurrentStatusEntity msgDroneStatus = new CurrentStatusEntity();
            msgDroneStatus.setDeviceId(droneControl.getDeviceId());
            currentAlt = ((float) globalPositionInt.relativeAlt()) / 1000;//将毫米转换成米
            currentLat = parseLat(globalPositionInt.lat());
            currentLng = parseLng(globalPositionInt.lon());
            msgDroneStatus.setAlt(currentAlt);
            msgDroneStatus.setLat(currentLat);
            msgDroneStatus.setLng(currentLng);
            int vx = globalPositionInt.vx() / 100;
            int vy = globalPositionInt.vy() / 100;
            msgDroneStatus.setHorizontalSpeed(Float.parseFloat(Convert.sqrt(vx, vy) + "")); //将cm转换成m
            msgDroneStatus.setVerticalSpeed((float) globalPositionInt.vz() / 100);    //将cm转换成m
            msgDroneStatus.setUpdateTime(DateTime.Now());
            updateOrInsertCurrentStatus(msgDroneStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 纬度处理
     *
     * @param lat
     * @return
     */
    private Double parseLat(Integer lat) {
        //除以10的7次方
        return lat / Math.pow(10, 7);
    }

    /**
     * 经度处理
     *
     * @param lng
     * @return
     */
    private Double parseLng(Integer lng) {
        //除以10的7次方
        return lng / Math.pow(10, 7);
    }


    private void proMsgVmountAttitude(IoSession session, MavlinkPacket packet) {
        try {
            //log.info("recv VmountAttitude:" + Convert.bytesToHexString(packet.getRawBytes(), true));
            //GlobalPositionInt globalPositionInt = (GlobalPositionInt) MavlinkUtil.packetToPayloadObject(packet);
            byte[] payload = packet.getPayload();
            if (payload != null && payload.length == 20) {
                //对payload进行校验查看是否有效
                //        mavlinkPacket.getPayload();
                //float roll = payload.getFloat();  //翻滚角
                //float pitch = payload.getFloat(); //俯仰角
                //float yaw = payload.getFloat();   //偏航角
                //port端口
                byte[] rollData = new byte[4];
                byte[] pitchData = new byte[4];
                byte[] yawData = new byte[4];
                System.arraycopy(payload, 8, rollData, 0, 4);
                System.arraycopy(payload, 12, pitchData, 0, 4);
                System.arraycopy(payload, 16, yawData, 0, 4);
                float roll = Convert.getFloat(rollData);
                float pitch = Convert.getFloat(pitchData);
                float yaw = Convert.getFloat(yawData);
                //writeDataToFile(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss")+" "+"roll:"+roll+"pitch:"+pitch+"yaw:"+yaw,"vmountAttitude.txt");

                //封装数据库操作bean
                CurrentStatusEntity msgDroneStatus = new CurrentStatusEntity();
                msgDroneStatus.setDeviceId(droneControl.getDeviceId());
                msgDroneStatus.setGimbalRoll(roll);
                msgDroneStatus.setGimbalPitch(pitch);
                msgDroneStatus.setGimbalYaw(yaw);
                msgDroneStatus.setUpdateTime(DateTime.Now());
                updateOrInsertCurrentStatus(msgDroneStatus);
            } else {
                //writeDataToFile(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss")+" "+"payload数据为空","vmountAttitude.txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proMsgSysStatus(IoSession session, MavlinkPacket packet) {
        //MavlinkMessage<SysStatus> sysStatusMavlinkMessage = MavlinkPackParseUtil.bytesToMessageObject(data);
        //writeDataToFile(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss")+" "+sysStatusMavlinkMessage.toString(),"msgSysStatus.txt");
        try {
            //log.info("recv SysStatus:" + Convert.bytesToHexString(packet.getRawBytes(), true));
            SysStatus sysStatus = (SysStatus) MavlinkUtil.packetToPayloadObject(packet);
            //封装数据库操作bean
            CurrentStatusEntity msgDroneStatus = new CurrentStatusEntity();
            msgDroneStatus.setDeviceId(droneControl.getDeviceId());
            msgDroneStatus.setBattery((float) sysStatus.batteryRemaining());
            msgDroneStatus.setUpdateTime(DateTime.Now());
            updateOrInsertCurrentStatus(msgDroneStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proMsgGpsRawInt(IoSession session, MavlinkPacket packet) {
        //MavlinkMessage<SysStatus> sysStatusMavlinkMessage = MavlinkPackParseUtil.bytesToMessageObject(data);
        //writeDataToFile(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss")+" "+sysStatusMavlinkMessage.toString(),"msgSysStatus.txt");
        try {
            //log.info("recv GpsRawInt:" + Convert.bytesToHexString(packet.getRawBytes(), true));
            GpsRawInt gpsRawInt = (GpsRawInt) MavlinkUtil.packetToPayloadObject(packet);
            //封装数据库操作bean
            CurrentStatusEntity msgDroneStatus = new CurrentStatusEntity();
//            msgDroneStatus.setDeviceId(droneControl.getDeviceId());
//            msgDroneStatus.setBattery((float) gpsRawInt.batteryRemaining());
//            msgDroneStatus.setUpdateTime(DateTime.Now());
//            updateOrInsertCurrentStatus(msgDroneStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proMsgParamValue(IoSession session, MavlinkPacket packet) {
        try {
            ParamValue paramValue = (ParamValue) MavlinkUtil.packetToPayloadObject(packet);
            paramList.put(paramValue.paramId(), paramValue.paramValue());

//            synchronized (synObject) {
//                if (paramSetId == paramValue.paramId() && paramValue.paramValue() == paramSetValue) {
//                    isOk = true;
//                } else {
//                    isOk = false;
//                }
//                synObject.notify();
//            }
//            log.info("Recv ParamValue.ParamId: " + paramValue.paramId() + " ParamValue:" + paramValue.paramValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void resetSeq() {
        try {
            synchronized (synObject) {
                seq = 0;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private int getSequence() {
        int rt = 0;
        try {
            synchronized (synObject) {
                ++seq;
                if (seq > 255) {
                    seq = 0;
                }
            }
            rt = seq;
        } catch (Exception e) {
            // TODO: handle exception
            rt = 0;
        }
        return rt;
    }

    //更新无人机的实时状态
    public void updateOrInsertCurrentStatus(CurrentStatusEntity currentStatusEntity) {
        try {
            SqlSession sqlSession = MybatisUtil.getSqlSession();
            //每一台无人机对应着一条实时记录
            CurrentStatusDao currentStatusDao = sqlSession.getMapper(CurrentStatusDao.class);
            List<CurrentStatusEntity> list = currentStatusDao.selectByDeviceId(currentStatusEntity.getDeviceId());
            if (list.size() > 0) {
                currentStatusEntity.setId(list.get(0).getId());
                currentStatusDao.updateById(currentStatusEntity);
            } else {
                currentStatusDao.insert(currentStatusEntity);
            }
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    public String getSpeedParamByDeviceType(int deviceType) {
        String paramSpeed = "WP_IDLE_VEL";
        try {
            switch (deviceType) {
                case DeviceType.DRONE:
                    paramSpeed = "WP_IDLE_VEL";
                    break;
                case DeviceType.BOAT:
                    paramSpeed = "CRUISE_SPEED";
                    break;
                default:
                    paramSpeed = "WP_IDLE_VEL";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paramSpeed;
    }

    //执行commandLong指令
    private boolean exeCmdLong(IoSession session, CommandLong cmd) {
        boolean rt = false;
        try {
            synchronized (synCmdObject) {
                cmdResult = MavResult.MAV_RESULT_FAILED;
                int confirmation = 0;
                int msgSeq = getSequence();
                int systemId = Config.gcsSystemId;
                int componentId = MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;
                IoBuffer buffer = IoBuffer.allocate(300);
                byte[] sendData = null;
                for (int i = 0; i < retries; i++) {
                    //最多尝试发送5次
                    try {
                        CommandLong commandLong = CommandLong.builder()
                                .targetSystem(cmd.targetSystem())
                                .targetComponent(cmd.targetComponent())
                                .command(cmd.command())
                                .confirmation(i) //confirmation自动加1
                                .param1(cmd.param1())
                                .param2(cmd.param2())
                                .param3(cmd.param3())
                                .param4(cmd.param4())
                                .param5(cmd.param5())
                                .param6(cmd.param6())
                                .param7(cmd.param7())
                                .build();
                        buffer.clear();
                        sendData = MavlinkUtil.payloadObjectToRawBytes(commandLong, msgSeq, systemId, componentId, mavlink_version);
                        buffer.put(sendData);
                        buffer.flip();
                        synchronized (synObject) {
                            isOk = false;
                            session.write(buffer);
                            log.info("Send:" + Convert.bytesToHexString(sendData, true));
                            //等待回复
                            synObject.wait(msgTimeout);
                            if (isOk) {
                                if (cmdResult.equals(MavResult.MAV_RESULT_ACCEPTED)) {
                                    rt = true;
                                } else {
                                    rt = false;
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public boolean setParam(IoSession session, ParamSet paramSet) {
        boolean rt = false;
        try {
            IoBuffer buffer = IoBuffer.allocate(300);
            byte[] sendData = null;
            int msgSeq = getSequence();
            int systemId = Config.gcsSystemId;
            int componentId = MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;
            //设置指定参数
            sendData = MavlinkUtil.payloadObjectToRawBytes(paramSet, msgSeq, systemId, componentId, mavlink_version);
            for (int i = 0; i < retries; i++) {
                try {
                    //最多尝试发送5次
                    buffer.clear();
                    buffer.put(sendData);
                    buffer.flip();
                    session.write(buffer);
                    log.info("Send:" + Convert.bytesToHexString(sendData, true));
                    //等待超时，查看新的参数值是否正确
                    Thread.sleep(msgTimeout);
                    if (paramList.containsKey(paramSet.paramId())) {
                        if (paramList.get(paramSet.paramId()).floatValue() == paramSet.paramValue()) {
                            rt = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }


    //发送setmode消息
    private boolean setMode(IoSession session, SetMode setMode) {
        boolean rt = false;
        try {
            synchronized (synCmdObject) {
                int msgSeq = getSequence();
                int systemId = Config.gcsSystemId;
                int componentId = MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;
                IoBuffer buffer = IoBuffer.allocate(300);
                byte[] sendData = null;
                buffer.clear();
                sendData = MavlinkUtil.payloadObjectToRawBytes(setMode, msgSeq, systemId, componentId, mavlink_version);
                buffer.put(sendData);
                buffer.flip();
                synchronized (synObject) {
                    session.write(buffer);
                    log.info("Send:" + Convert.bytesToHexString(sendData, true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //上传航点飞行任务
    public boolean uploadMission(IoSession session, PointFlyContent content) {
        boolean rt = false;

        try {
            //尝试清理航线
            int targetSystemId = CommandHandler.getSysIdByDeviceId(content.getDeviceId(), content.getSysId());
            int targetComponentId = MAV_COMPONENT.MAV_COMP_ID_ALL;
            clearMission(session, targetSystemId, targetComponentId);
            synchronized (synMissionObject) {
                //初始化requestSeq和missionResult值
                requestSeq = 0;

                missionResult = MavMissionResult.MAV_MISSION_INVALID;
                missionRequestType = MissionRequestType.MISSION_REQUEST_INT; //新版本协议均采用mission_request_int消息代替mission_request消息
                int msgSeq = getSequence();
                int systemId = Config.gcsSystemId;
                int componentId = MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;

                int itemCount = content.getFlightContent().size();
                IoBuffer buffer = IoBuffer.allocate(300);
                byte[] sendData = null;

                //设置任务速度
                boolean isSetParam = false;
                switch (content.getDeviceType()) {
                    case DeviceType.DRONE:
                        //若是无人机，发送设置速度参数WP_IDLE_VEL
                        isSetParam = setParam(session, ParamSet.builder()
                                .paramId("WP_IDLE_VEL")
                                .paramValue(content.getSpeed())
                                .paramType(MavParamType.MAV_PARAM_TYPE_REAL32)
                                .targetSystem(targetSystemId)
                                .targetComponent(targetComponentId)
                                .build());
                        break;
                    case DeviceType.BOAT:
                        //若是无人船，需要同时设置CRUISE_SPEED和WP_SPEED
                        boolean isSetOk = false;
                        isSetOk = setParam(session, ParamSet.builder()
                                .paramId("CRUISE_SPEED")
                                .paramValue(content.getSpeed())
                                .paramType(MavParamType.MAV_PARAM_TYPE_REAL32)
                                .targetSystem(targetSystemId)
                                .targetComponent(targetComponentId)
                                .build());
                        if (!isSetOk) {
                            isSetParam = false;
                        } else {
                            isSetParam = setParam(session, ParamSet.builder()
                                    .paramId("WP_SPEED")
                                    .paramValue(content.getSpeed())
                                    .paramType(MavParamType.MAV_PARAM_TYPE_REAL32)
                                    .targetSystem(targetSystemId)
                                    .targetComponent(targetComponentId)
                                    .build());
                        }
                        break;
                    default:
                        break;
                }
                if (!isSetParam) {
                    rt = false;
                    return rt;
                }
                //先发送MISSION_COUNT消息
                MissionCount missionCount = MissionCount.builder()
                        .targetSystem(targetSystemId)
                        .targetComponent(targetComponentId)
                        .count(itemCount)
                        .missionType(MavMissionType.MAV_MISSION_TYPE_MISSION)
                        .build();
                sendData = MavlinkUtil.payloadObjectToRawBytes(missionCount, msgSeq, systemId, componentId, mavlink_version);
                boolean isSendCount = false;
                for (int i = 0; i < retries; i++) {
                    try {
                        //最多尝试发送5次
                        buffer.clear();
                        buffer.put(sendData);
                        buffer.flip();
                        synchronized (synObject) {
                            isOk = false;
                            session.write(buffer);
                            log.info("Send:" + Convert.bytesToHexString(sendData, true));
                            //等待回复
                            synObject.wait(msgTimeout);
                            if (isOk) {
                                isSendCount = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (isSendCount) {
                    //等待上传mission_item，在无人机返回的mission_request_int消息和mission_ack消息中进行处理
                    for (int i = 0; i < content.getFlightContent().size(); i++) {
                        Waypoint waypoint = content.getFlightContent().get(i);
                        msgSeq = getSequence();
                        if (missionRequestType == MissionRequestType.MISSION_REQUEST_INT) {
//                            if (i == 0) {
//                                //第一个指令为起飞nav_takeoff，后面的为nav_waypoint
//                                MissionItemInt missionItemInt = MissionItemInt.builder()
//                                        .targetSystem(targetSystemId)
//                                        .targetComponent(targetComponentId)
//                                        .seq(requestSeq)
//                                        .command(MavCmd.MAV_CMD_NAV_TAKEOFF)
//                                        .frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT)
//                                        .autocontinue(1)
//                                        .param1(0)
//                                        .param2(0)
//                                        .param3(0)
//                                        .param4(Float.NaN)
//                                        .x((int) (waypoint.getLat() * Math.pow(10, 7)))
//                                        .y((int) (waypoint.getLng() * Math.pow(10, 7)))
//                                        .z(waypoint.getAlt())
//                                        .missionType(MavMissionType.MAV_MISSION_TYPE_MISSION)
//                                        .build();
//                                buffer.clear();
//                                sendData = MavlinkUtil.payloadObjectToRawBytes(missionItemInt, msgSeq, systemId, componentId, mavlink_version);
//                                buffer.put(sendData);
//                            } else {
                            MissionItemInt missionItemInt = MissionItemInt.builder()
                                    .targetSystem(targetSystemId)
                                    .targetComponent(targetComponentId)
                                    .seq(requestSeq)
                                    .command(MavCmd.MAV_CMD_NAV_WAYPOINT)
                                    .frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT)
                                    .autocontinue(1)
                                    .param1(0)
                                    .param2(0)
                                    .param3(0)
                                    .param4(Float.NaN)
                                    .x((int) (waypoint.getLat() * Math.pow(10, 7)))
                                    .y((int) (waypoint.getLng() * Math.pow(10, 7)))
                                    .z(waypoint.getAlt())
                                    .missionType(MavMissionType.MAV_MISSION_TYPE_MISSION)
                                    .build();
                            sendData = MavlinkUtil.payloadObjectToRawBytes(missionItemInt, msgSeq, systemId, componentId, mavlink_version);
//                            }
                        } else if (missionRequestType == MissionRequestType.MISSION_REQUEST) {
//                            if (i == 0) {
//                                MissionItem missionItem = MissionItem.builder()
//                                        .targetSystem(targetSystemId)
//                                        .targetComponent(targetComponentId)
//                                        .seq(requestSeq)
//                                        .command(MavCmd.MAV_CMD_NAV_TAKEOFF)
//                                        .frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT)
//                                        .autocontinue(1)
//                                        .param1(0)
//                                        .param2(0)
//                                        .param3(0)
//                                        .param4(Float.NaN)
//                                        .x((float) waypoint.getLat())
//                                        .y((float) waypoint.getLng())
//                                        .z(waypoint.getAlt())
//                                        .missionType(MavMissionType.MAV_MISSION_TYPE_MISSION)
//                                        .build();
//                                buffer.clear();
//                                sendData=MavlinkUtil.payloadObjectToRawBytes(missionItem, msgSeq, systemId, componentId, mavlink_version);
//                                buffer.put(sendData);
//                            } else {
                            MissionItem missionItem = MissionItem.builder()
                                    .targetSystem(targetSystemId)
                                    .targetComponent(targetComponentId)
                                    .seq(requestSeq)
                                    .command(MavCmd.MAV_CMD_NAV_WAYPOINT)
                                    .frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT)
                                    .autocontinue(1)
                                    .param1(0)
                                    .param2(0)
                                    .param3(0)
                                    .param4(Float.NaN)
                                    .x((float) waypoint.getLat())
                                    .y((float) waypoint.getLng())
                                    .z(waypoint.getAlt())
                                    .missionType(MavMissionType.MAV_MISSION_TYPE_MISSION)
                                    .build();
                            sendData = MavlinkUtil.payloadObjectToRawBytes(missionItem, msgSeq, systemId, componentId, mavlink_version);
//                            }
                        }
                        for (int j = 0; j < retries; j++) {
                            //最多尝试发送5次
                            if (i == requestSeq) {
                                //发送当前的mission_item
                                try {
                                    buffer.clear();
                                    buffer.put(sendData);
                                    buffer.flip();
                                    //等待下一个mission_request请求
                                    synchronized (synObject) {
                                        isOk = false;
                                        session.write(buffer);
                                        log.info("Send:" + Convert.bytesToHexString(sendData, true));
                                        synObject.wait(missionItemTimeout);
                                        if (isOk) {
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                //说明收到了新的mission_request，继续发送下一个item
                                break;
                            }
                        }
                    }
                    //等待mission_ack，判断上传任务结果
                    try {
                        //等待下一个mission_request请求
                        synchronized (synObject) {
                            synObject.wait(msgTimeout);
                            if (missionResult.equals(MavMissionResult.MAV_MISSION_ACCEPTED)) {
                                //上传任务成功
                                rt = true;
                            } else {
                                rt = false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    rt = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //开始执行航点飞行任务
    public boolean startMission(IoSession session, int targetSystemId, int targetComponentId, int missionCount) {
        boolean rt = false;
        try {
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(MavCmd.MAV_CMD_MISSION_START)
                    .confirmation(0) //confirmation自动加1
                    .param1(0)
                    .param2(missionCount - 1)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public boolean stopMission(IoSession session, int targetSystemId, int targetComponentId) {
        return pauseMission(session, targetSystemId, targetComponentId);
    }

    public boolean pauseMission(IoSession session, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(MavCmd.MAV_CMD_DO_PAUSE_CONTINUE)
                    .confirmation(0)
                    .param1(0)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public boolean resumeMission(IoSession session, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(MavCmd.MAV_CMD_DO_PAUSE_CONTINUE)
                    .confirmation(0)
                    .param1(1)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public boolean goHome(IoSession session, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(MavCmd.MAV_CMD_NAV_RETURN_TO_LAUNCH)
                    .confirmation(0)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public boolean sendCollaboration(IoSession session, int targetSystemId, int targetComponentId, int collaboration) {
        boolean rt = false;
        try {
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(EnumValue.create(6102))
                    .confirmation(0)
                    .param1(collaboration)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public boolean setCollaborationValue(IoSession session, int targetSystemId, int targetComponentId, int collaborationValue) {
        boolean rt = false;
        try {
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(EnumValue.create(6104))
                    .confirmation(0)
                    .param1(collaborationValue)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public boolean setHold(IoSession session, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            //rt = pauseMission(session, targetSystemId, targetComponentId);
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(MavCmd.MAV_CMD_DO_SET_MODE)
                    .confirmation(0)
                    .param1(0x9d)
                    .param2(4)
                    .param3(3)
                    .param4(0)
                    .param5(0)
                    .param6(0)
                    .param7(0)
                    .build();
            rt = exeCmdLong(session, commandLong);
//            SetMode setMode = SetMode.builder()
//                    .targetSystem(targetSystemId)
//                    .baseMode(EnumValue.create(0x9d))
//                    .customMode(0x03040000)
//                    .build();
//            rt = setMode(session, setMode);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //指点飞行，飞到指定的GPS点位
    public boolean gotoPosition(IoSession session, int targetSystemId, int targetComponentId, double lat, double lng, float alt) {
        boolean rt = false;
        try {
            //先发送hold指令，再发送reposition指令
            rt = setHold(session, targetSystemId, targetComponentId);
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(MavCmd.MAV_CMD_DO_REPOSITION)
                    .confirmation(0)
                    .param1(0)
                    .param2(0)
                    .param3(0)
                    .param4(0)
                    .param5((float) lat)
                    .param6((float) lng)
                    .param7(alt)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //起飞到指定高度
    public boolean takeoff(IoSession session, int targetSystemId, int targetComponentId, double lat, double lng, float alt) {
        boolean rt = false;
        try {
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(MavCmd.MAV_CMD_NAV_TAKEOFF)
                    .confirmation(0)
                    .param1(0)
                    .param2(0)
                    .param3(0)
                    .param4(Float.NaN)
                    .param5((float) lat)
                    .param6((float) lng)
                    .param7(alt)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //起飞到指定高度，采用当前经纬度
    public boolean takeoff(IoSession session, int targetSystemId, int targetComponentId, float alt) {
        return takeoff(session, targetSystemId, targetComponentId, currentLat, currentLng, alt);
    }

    public boolean restartDevice(IoSession session, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            CommandLong commandLong = CommandLong.builder()
                    .targetSystem(targetSystemId)
                    .targetComponent(targetComponentId)
                    .command(MavCmd.MAV_CMD_PREFLIGHT_REBOOT_SHUTDOWN)
                    .confirmation(0)
                    .param1(1)
                    .param2(0)
                    .param3(0)
                    .param4(0)
                    .param5(0)
                    .param6(0)
                    .param7(0)
                    .build();
            rt = exeCmdLong(session, commandLong);
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //清除指定设备的mission航线信息
    public boolean clearMission(IoSession session, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            synchronized (synMissionObject) {
                missionResult = MavMissionResult.MAV_MISSION_INVALID;
                int msgSeq = getSequence();
                int systemId = Config.gcsSystemId;
                int componentId = MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;
                IoBuffer buffer = IoBuffer.allocate(300);
                byte[] sendData = null;
                MissionClearAll missionClearAll = MissionClearAll.builder()
                        .targetSystem(targetSystemId)
                        .targetComponent(targetComponentId)
                        .missionType(MavMissionType.MAV_MISSION_TYPE_MISSION)
                        .build();
                for (int i = 0; i < retries; i++) {
                    //最多尝试发送5次
                    try {
                        buffer.clear();
                        sendData = MavlinkUtil.payloadObjectToRawBytes(missionClearAll, msgSeq, systemId, componentId, mavlink_version);
                        buffer.put(sendData);
                        buffer.flip();
                        synchronized (synObject) {
                            isOk = false;
                            session.write(buffer);
                            log.info("Send:" + Convert.bytesToHexString(sendData, true));
                            //等待回复
                            synObject.wait(msgTimeout);
                            if (missionResult.equals(MavMissionResult.MAV_MISSION_ACCEPTED)) {
                                //清理航线成功
                                rt = true;
                                break;
                            } else {
                                rt = false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }
}
