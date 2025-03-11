package com.hkxx.drone;

import com.MAVLink.enums.MAV_COMPONENT;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hkxx.drone.common.Convert;
import com.hkxx.drone.common.DateTime;
import com.hkxx.drone.common.TcpClient;
import com.hkxx.drone.db.MybatisUtil;
import com.hkxx.drone.db.dao.*;
import com.hkxx.drone.db.entity.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CommandHandler extends IoHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(CommandHandler.class);
    int tsWait = 5000; // 等待信号量事件通知的时间，单位毫秒

    DroneService droneService = null;
    AttributeKey loginKey = new AttributeKey(getClass(), "loginKey"); //登录标识key
    AttributeKey visionLandKey = new AttributeKey(getClass(), "visionLandKey"); //订阅视觉降落的标识key
    AttributeKey appControlKey = new AttributeKey(getClass(), "appControlKey"); //app远程控制的标识key
    TcpClient parentClient = null; //连接上级通信服务器的客户端连接对象
    CommandService commandService = null; //管理命令服务对象引用

    public DroneService getDroneService() {
        return droneService;
    }

    public void setDroneService(DroneService droneService) {
        this.droneService = droneService;
    }

    public CommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(CommandService commandService) {
        this.commandService = commandService;
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

            // 采用ASCII解码
            msg = new String(data, StandardCharsets.UTF_8);
            if (msg.equals("{}\r\n")) {
                return;
            }
            log.info("Recv:" + msg);
            //解析cmd命令，根据不同的命令类型执行不同操作
            Command command = JSON.parseObject(msg, Command.class);
            if (command != null) {
                switch (command.getCmd()) {
                    case CommandType.UPLOAD_TASK:
                        uploadTask(session, command);
                        break;
                    case CommandType.START_MISSION:
                        startMission(session, command);
                        break;
                    case CommandType.STOP_MISSION:
                        stopMission(session, command);
                        break;
                    case CommandType.PAUSE_MISSION:
                        pauseMission(session, command);
                        break;
                    case CommandType.RESUME_MISSION:
                        resumeMission(session, command);
                        break;
                    case CommandType.GO_HOME:
                        goHome(session, command);
                        break;
                    case CommandType.GOTO_POINT:
                        gotoPoint(session, command);
                        break;
                    case CommandType.SHOOT_PHOTO:
                        shootPhoto(session, command);
                        break;
                    case CommandType.CAMERA_CENTER:
                        cameraCenter(session, command);
                        break;
                    case CommandType.CAMERA_CHANGE_MODE:
                        changeCameraMode(session, command);
                        break;
                    case CommandType.TRIGGER_VIDEO:
                        triggerVideo(session, command);
                        break;
                    case CommandType.IRC_NIGHT:
                        ircNight(session, command);
                        break;
                    case CommandType.IRC_DAY:
                        ircDay(session, command);
                        break;
                    case CommandType.IRC_AUTO:
                        ircAuto(session, command);
                        break;
                    case CommandType.ZOOM_PLUS:
                        zoomPlus(session, command);
                        break;
                    case CommandType.ZOOM_MINUS:
                        zoomMinus(session, command);
                        break;
                    case CommandType.FOCUS_PLUS:
                        focusPlus(session, command);
                        break;
                    case CommandType.FOCUS_MINUS:
                        focusMinus(session, command);
                        break;
                    case CommandType.FOCUS_AUTO:
                        focusAuto(session, command);
                        break;
                    case CommandType.CAMERA_LOCKUP:
                        cameraLockup(session, command);
                        break;
                    case CommandType.CAMERA_FOLLOW:
                        cameraFollow(session, command);
                        break;
                    case CommandType.CAMERA_TRACK:
                        cameraTrack(session, command);
                        break;
                    case CommandType.MOVE_GIMBAL:
                        moveGimbal(session, command);
                        break;
                    case CommandType.OPEN_THROWER:
                        openThrower(session, command);
                        break;
                    case CommandType.CLOSE_THROWER:
                        closeThrower(session, command);
                        break;
                    case CommandType.OPEN_LIGHT:
                        openLight(session, command);
                        break;
                    case CommandType.CLOSE_LIGHT:
                        closeLight(session, command);
                        break;
                    case CommandType.MOVE_LIGHT:
                        moveLight(session, command);
                        break;
                    case CommandType.NOTIFY_STATUS:
                        notifyStatus(session, command);
                        break;
                    case CommandType.LOGIN:
                        login(session, command);
                        break;
                    case CommandType.LOGIN_PARENT:
                        loginParent(session, command);
                        break;
                    case CommandType.SEND_TASK:
                        sendTask(session, command);
                        break;
                    case CommandType.SEND_INSTRUCTION:
                        sendInstruction(session, command);
                        break;
                    case CommandType.MANUAL_CONTROL:
                        manualControl(session, command);
                        break;
                    case CommandType.VISION_LAND:
                        visionLand(session, command);
                        break;
                    case CommandType.SUBSCRIBE_VISION_LAND:
                        subscribeVisionLand(session, command);
                        break;
                    case CommandType.DETECT_STATUS:
                        break;
                    case CommandType.TRACK_STATUS:
                        break;
                    case CommandType.NOTIFY_USER_STATUS:
                        notifyUserStatus(session, command);
                        break;
                    case CommandType.DETECTED_INFO:
                        detectedInfo(session, command);
                        break;
                    case CommandType.AIS_STATUS:
                        aisStatus(session, command);
                        break;
                    case CommandType.SUBSCRIBE_APP_CONTROL:
                        subscribeAppControl(session, command);
                        break;
                    default:
                        // 不做处理
                        break;
                }
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

    public static int getSysIdByDeviceId(int deviceId, int sysId) {
        int rt = 0;
        try {
            //若deviceId为0，则返回sysId，否则查询系统中该设备对应的sysId
            if (deviceId == 0) {
                rt = sysId;
            } else {
                DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
                DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
                if (droneEntity != null) {
                    rt = droneEntity.getTargetid();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = 0;
        } finally {
            MybatisUtil.close();
        }
        return rt;
    }

    public static int getDeviceIdBySysId(int deviceId, int sysId) {
        int rt = 0;
        try {
            //若deviceId为0，则根据sysId查询系统中该设备对应的deviceId
            if (deviceId == 0) {
                DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
                DroneEntity droneEntity = droneDao.selectBySysId(sysId);
                if (droneEntity != null) {
                    rt = droneEntity.getDeviceId();
                }
            } else {
                rt = deviceId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = 0;
        } finally {
            MybatisUtil.close();
        }
        return rt;
    }

    //上传任务
    private void uploadTask(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            if (droneService != null) {
                TaskInfo taskInfo = JSON.toJavaObject((JSON) command.getParam(), TaskInfo.class);
                boolean rt = false;
                switch (taskInfo.getTaskType()) {
                    case TaskType.POINT_FLY:
                        PointFlyContent pointFlyContent = JSON.toJavaObject((JSON) taskInfo.getContent(), PointFlyContent.class);
                        rt = droneService.uploadMission(getDeviceIdBySysId(pointFlyContent.getDeviceId(), pointFlyContent.getSysId()), pointFlyContent);
                        break;
                    case TaskType.AREA_FLY:
                        break;
                    case TaskType.PANORAMA_FLY:
                        break;
                    case TaskType.CLUSTER_FLY:
                        ClusterFlyContent clusterFlyContent = JSON.toJavaObject((JSON) taskInfo.getContent(), ClusterFlyContent.class);
                        rt = droneService.uploadClusterMission(clusterFlyContent);
                        break;
                    default:
                        break;
                }
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Upload task is success.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Upload task is failed.");
                }
            } else {
                commandResult.setCode(500);
                commandResult.setResult("Drone service is not alive.");
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //开始执行任务
    private void startMission(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
            boolean rt = false;
            //根据task_id查询任务类型等参数，执行任务
            TaskDao taskDao = MybatisUtil.getSqlSession().getMapper(TaskDao.class);
            TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
            DeviceInfo deviceInfo = null;
            deviceInfo = missionParam.getDevice().get(0);
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = deviceInfo.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Start mission success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Start mission failed.");
                }
            } else {
                if (droneService != null) {
                    switch (taskEntity.getTaskType()) {
                        case TaskTypeInt.POINT_FLY:
                            //获取航点飞行任务的航线信息，计算航点数量，执行航点飞行任务
                            FlightpathDao flightpathDao = (FlightpathDao) MybatisUtil.getSqlSession().getMapper(FlightpathDao.class);
                            List<FlightpathEntity> flightpathEntities = flightpathDao.selectByTaskId(missionParam.getTaskId());
                            List<Waypoint> content = JSON.parseArray(flightpathEntities.get(0).getContent(), Waypoint.class);
                            int missionCount = content.size();
                            deviceInfo = missionParam.getDevice().get(0);
                            rt = droneService.startMission(deviceInfo.getDeviceId(), deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, missionCount);
                            break;
                        case TaskTypeInt.AREA_FLY:
                            break;
                        case TaskTypeInt.PANORAMA_FLY:
                            break;
                        case TaskTypeInt.CLUSTER_FLY:
                            rt = droneService.startClusterMission(missionParam);
                            break;
                        default:
                            break;
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Start mission success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Start mission failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //停止执行任务
    private void stopMission(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
            boolean rt = false;
            //根据task_id查询任务类型等参数，执行任务
            TaskDao taskDao = MybatisUtil.getSqlSession().getMapper(TaskDao.class);
            TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
            DeviceInfo deviceInfo = null;
            deviceInfo = missionParam.getDevice().get(0);
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = deviceInfo.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Stop mission success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Stop mission failed.");
                }
            } else {
                if (droneService != null) {
                    switch (taskEntity.getTaskType()) {
                        case TaskTypeInt.POINT_FLY:
                            //停止执行航点飞行任务
                            deviceInfo = missionParam.getDevice().get(0);
                            rt = droneService.stopMission(deviceInfo.getDeviceId(), deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_ALL);
                            break;
                        case TaskTypeInt.AREA_FLY:
                            break;
                        case TaskTypeInt.PANORAMA_FLY:
                            break;
                        case TaskTypeInt.CLUSTER_FLY:
                            rt = droneService.stopClusterMission(missionParam);
                            break;
                        default:
                            break;
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Stop mission success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Stop mission failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //暂停任务
    private void pauseMission(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
            boolean rt = false;
            //根据task_id查询任务类型等参数，执行任务
            TaskDao taskDao = (TaskDao) MybatisUtil.getSqlSession().getMapper(TaskDao.class);
            TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
            DeviceInfo deviceInfo = null;
            deviceInfo = missionParam.getDevice().get(0);
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = deviceInfo.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Pause mission success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Pause mission failed.");
                }
            } else {
                if (droneService != null) {
                    switch (taskEntity.getTaskType()) {
                        case TaskTypeInt.POINT_FLY:
                            //暂停执行航点飞行任务
                            deviceInfo = missionParam.getDevice().get(0);
                            rt = droneService.pauseMission(deviceInfo.getDeviceId(), deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_ALL);
                            break;
                        case TaskTypeInt.AREA_FLY:
                            break;
                        case TaskTypeInt.PANORAMA_FLY:
                            break;
                        case TaskTypeInt.CLUSTER_FLY:
                            rt = droneService.pauseClusterMission(missionParam);
                            break;
                        default:
                            break;
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Pause mission success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Pause mission failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //恢复执行任务
    private void resumeMission(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
            boolean rt = false;
            //根据task_id查询任务类型等参数，执行任务
            TaskDao taskDao = (TaskDao) MybatisUtil.getSqlSession().getMapper(TaskDao.class);
            TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
            DeviceInfo deviceInfo = null;
            deviceInfo = missionParam.getDevice().get(0);
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = deviceInfo.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Resume mission success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Resume mission failed.");
                }
            } else {
                if (droneService != null) {
                    switch (taskEntity.getTaskType()) {
                        case TaskTypeInt.POINT_FLY:
                            //暂停执行航点飞行任务
                            deviceInfo = missionParam.getDevice().get(0);
                            rt = droneService.resumeMission(deviceInfo.getDeviceId(), deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_ALL);
                            break;
                        case TaskTypeInt.AREA_FLY:
                            break;
                        case TaskTypeInt.PANORAMA_FLY:
                            break;
                        case TaskTypeInt.CLUSTER_FLY:
                            rt = droneService.resumeClusterMission(missionParam);
                            break;
                        default:
                            break;
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Resume mission success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Resume mission failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //返航
    private void goHome(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
            boolean rt = false;
            //根据task_id查询任务类型等参数，执行任务
            TaskDao taskDao = (TaskDao) MybatisUtil.getSqlSession().getMapper(TaskDao.class);
            TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
            DeviceInfo deviceInfo = null;
            deviceInfo = missionParam.getDevice().get(0);
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = deviceInfo.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("GoHome success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("GoHome failed.");
                }
            } else {
                if (droneService != null) {
                    switch (taskEntity.getTaskType()) {
                        case TaskTypeInt.POINT_FLY:
                            //暂停执行航点飞行任务
                            deviceInfo = missionParam.getDevice().get(0);
                            rt = droneService.goHome(deviceInfo.getDeviceId(), deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_ALL);
                            break;
                        case TaskTypeInt.AREA_FLY:
                            break;
                        case TaskTypeInt.PANORAMA_FLY:
                            break;
                        case TaskTypeInt.CLUSTER_FLY:
                            rt = droneService.goHomeCluster(missionParam);
                            break;
                        default:
                            break;
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("GoHome success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("GoHome failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //指点飞行
    private void gotoPoint(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            boolean rt = false;
            GotoPointParam gotoPointParam = JSON.toJavaObject((JSON) command.getParam(), GotoPointParam.class);
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = gotoPointParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Goto point success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Goto point failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.gotoPosition(gotoPointParam.getDeviceId(),
                            gotoPointParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1,
                            gotoPointParam.getLat(), gotoPointParam.getLng(), gotoPointParam.getAlt());
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Goto point success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Goto point failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //拍照
    private void shootPhoto(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Shoot photo success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Shoot photo failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.shootPhoto(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Shoot photo success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Shoot photo failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //回中
    private void cameraCenter(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Camera center success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Camera center failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.cameraCenter(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Camera center success.");

                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Camera center failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //切换相机模式
    private void changeCameraMode(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Change camera mode success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Change camera mode failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.changeCameraMode(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Change camera mode success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Change camera mode failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //开始或停止录像
    private void triggerVideo(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Trigger video success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Trigger video failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.triggerVideo(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("Trigger video success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("Trigger video failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //夜视模式
    private void ircNight(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("IrcNight success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("IrcNight failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.ircNight(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("IrcNight success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("IrcNight failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //白天模式
    private void ircDay(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("IrcDay success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("IrcDay failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.ircDay(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("IrcDay success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("IrcDay failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //自动切换黑夜模式
    private void ircAuto(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("IrcAuto success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("IrcAuto failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.ircAuto(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("IrcAuto success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("IrcAuto failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //放大
    private void zoomPlus(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("ZoomPlus success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("ZoomPlus failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.zoomPlus(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("ZoomPlus success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("ZoomPlus failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //缩小
    private void zoomMinus(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("ZoomMinus success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("ZoomMinus failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.zoomMinus(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("ZoomMinus success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("ZoomMinus failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //增加焦距
    private void focusPlus(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("FocusPlus success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("FocusPlus failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.focusPlus(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("FocusPlus success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("FocusPlus failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //减小焦距
    private void focusMinus(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("FocusMinus success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("FocusMinus failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.focusMinus(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("FocusMinus success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("FocusMinus failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //自动对焦
    private void focusAuto(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("FocusAuto success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("FocusAuto failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.focusAuto(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("FocusAuto success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("FocusAuto failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //朝向模式锁头
    private void cameraLockup(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("CameraLockUp success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("CameraLockUp failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.cameraLockup(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("CameraLockUp success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("CameraLockUp failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //朝向模式跟随
    private void cameraFollow(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("CameraFollow success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("CameraFollow failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.cameraFollow(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("CameraFollow success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("CameraFollow failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //跟踪
    private void cameraTrack(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DeviceInfo cameraParam = JSON.toJavaObject((JSON) command.getParam(), DeviceInfo.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = cameraParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("CameraTrack success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("CameraTrack failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.cameraTrack(cameraParam.getDeviceId(),
                            cameraParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("CameraTrack success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("CameraTrack failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //转动云台
    private void moveGimbal(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            MoveGimbalParam moveGimbalParam = JSON.toJavaObject((JSON) command.getParam(), MoveGimbalParam.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = moveGimbalParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("MoveGimbal success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("MoveGimbal failed.");
                }
            } else {
                if (droneService != null) {
                    switch (moveGimbalParam.getDirection()) {
                        case MoveDirection.LEFT:
                            rt = droneService.moveGimbal(moveGimbalParam.getDeviceId(),
                                    moveGimbalParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 0, -1);
                            break;
                        case MoveDirection.RIGHT:
                            rt = droneService.moveGimbal(moveGimbalParam.getDeviceId(),
                                    moveGimbalParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 0, 1);
                            break;
                        case MoveDirection.UP:
                            rt = droneService.moveGimbal(moveGimbalParam.getDeviceId(),
                                    moveGimbalParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, -1, 0);
                            break;
                        case MoveDirection.DOWN:
                            rt = droneService.moveGimbal(moveGimbalParam.getDeviceId(),
                                    moveGimbalParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 1, 0);
                            break;
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("MoveGimbal success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("MoveGimbal failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //打开抛投
    private void openThrower(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            SetChannelParam setChannelParam = JSON.toJavaObject((JSON) command.getParam(), SetChannelParam.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = setChannelParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("OpenThrower success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("OpenThrower failed.");
                }
            } else {
                if (droneService != null) {
                    if (setChannelParam.getChannel() == -1) {
                        //默认0号通道控制抛投
                        //通道状态置为1
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 1, 0);
                    } else {
                        //通道状态设置为1
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 1, setChannelParam.getChannel());
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("OpenThrower success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("OpenThrower failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //关闭抛投
    private void closeThrower(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            SetChannelParam setChannelParam = JSON.toJavaObject((JSON) command.getParam(), SetChannelParam.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = setChannelParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("CloseThrower success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("CloseThrower failed.");
                }
            } else {
                if (droneService != null) {
                    if (setChannelParam.getChannel() == -1) {
                        //默认0号通道控制抛投
                        //通道状态置为0
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 0, 0);
                    } else {
                        //通道状态设置为0
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 0, setChannelParam.getChannel());
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("CloseThrower success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("CloseThrower failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //打开照明
    private void openLight(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            SetChannelParam setChannelParam = JSON.toJavaObject((JSON) command.getParam(), SetChannelParam.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = setChannelParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("OpenLight success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("OpenLight failed.");
                }
            } else {
                if (droneService != null) {
                    if (setChannelParam.getChannel() == -1) {
                        //默认1号通道控制照明开关，0号通道控制照明角度
                        //先发送关闭指令
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 1, 1);
                        Thread.sleep(1000);
                        //再发送打开指令
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 0, 1);
                    } else {
                        //通道状态设置为1
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 1, setChannelParam.getChannel());
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("OpenLight success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("OpenLight failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //关闭照明
    private void closeLight(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            SetChannelParam setChannelParam = JSON.toJavaObject((JSON) command.getParam(), SetChannelParam.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = setChannelParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("CloseLight success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("CloseLight failed.");
                }
            } else {
                if (droneService != null) {
                    if (setChannelParam.getChannel() == -1) {
                        //默认1号通道控制照明开关，0号通道控制照明角度
                        //先发送关闭指令
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 1, 1);
                        Thread.sleep(1000);
                        //再发送打开指令
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 0, 1);
                    } else {
                        //通道状态设置为0
                        rt = droneService.setChannelState(setChannelParam.getDeviceId(),
                                setChannelParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 0, setChannelParam.getChannel());
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("CloseLight success.");

                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("CloseLight failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //转动照明灯
    private void moveLight(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            MoveGimbalParam moveGimbalParam = JSON.toJavaObject((JSON) command.getParam(), MoveGimbalParam.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = moveGimbalParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("MoveLight success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("MoveLight failed.");
                }
            } else {
                if (droneService != null) {
                    //默认1号通道控制照明开关，0号通道控制照明角度
                    switch (moveGimbalParam.getDirection()) {
                        case MoveDirection.UP:
                            rt = droneService.setChannelState(moveGimbalParam.getDeviceId(),
                                    moveGimbalParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 0, 0);
                            break;
                        case MoveDirection.DOWN:
                            rt = droneService.setChannelState(moveGimbalParam.getDeviceId(),
                                    moveGimbalParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 1, 0);
                            break;
                        default:
                            break;
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("MoveLight success.");

                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("MoveLight failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //上报通知无人机的状态，用于第三方无人机接入或者移动端控制无人机的数据状态接入
    private void notifyStatus(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            NotifyStatusParam notifyStatusParam = JSON.toJavaObject((JSON) command.getParam(), NotifyStatusParam.class);
            boolean rt = false;
            //根据name查找该设备对应的deviceId
            DeviceDao deviceDao = MybatisUtil.getSqlSession().getMapper(DeviceDao.class);
            DeviceEntity deviceEntity = deviceDao.selectByName(notifyStatusParam.getName());
            if (deviceEntity != null) {
                CurrentStatusEntity msgDroneStatus = new CurrentStatusEntity();
                msgDroneStatus.setDeviceId(deviceEntity.getId());
                msgDroneStatus.setAlt(notifyStatusParam.getAlt());
                msgDroneStatus.setLat(notifyStatusParam.getLat());
                msgDroneStatus.setLng(notifyStatusParam.getLng());
                msgDroneStatus.setHorizontalSpeed(notifyStatusParam.gethSpeed());
                msgDroneStatus.setVerticalSpeed(notifyStatusParam.getvSpeed());
                msgDroneStatus.setYaw(notifyStatusParam.getYaw());
                msgDroneStatus.setRoll(notifyStatusParam.getRoll());
                msgDroneStatus.setPitch(notifyStatusParam.getPitch());
                msgDroneStatus.setBattery(notifyStatusParam.getBattery());
                msgDroneStatus.setState(notifyStatusParam.getState());
                msgDroneStatus.setUpdateTime(DateTime.Now());
                MavlinkHandler.updateOrInsertCurrentStatus(msgDroneStatus);
                rt = true;
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("NotifyStatus ok.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("NotifyStatus failed.");
                }
            } else {
                commandResult.setCode(500);
                commandResult.setResult("Device is not registered.");
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }


    //上报通知无人机的状态，用于第三方无人机接入或者移动端控制无人机的数据状态接入
    private void notifyUserStatus(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            NotifyUserStatusParam notifyUserStatusParam = JSON.toJavaObject((JSON) command.getParam(), NotifyUserStatusParam.class);
            boolean rt = false;
            //根据name查找该设备对应的deviceId
            UserDao userDao = MybatisUtil.getSqlSession().getMapper(UserDao.class);
            UserEntity userEntity = userDao.selectById(notifyUserStatusParam.getUserId());
            if (userEntity != null) {
                //存在则更新状态，不存在则插入新状态
                UserStatusEntity userStatusEntity = new UserStatusEntity();
                userStatusEntity.setUserId(notifyUserStatusParam.getUserId());
                userStatusEntity.setUserName(notifyUserStatusParam.getName());
                userStatusEntity.setLat(notifyUserStatusParam.getLat());
                userStatusEntity.setLng(notifyUserStatusParam.getLng());
                userStatusEntity.setTaskId(notifyUserStatusParam.getTaskId());
                userStatusEntity.setUpdateTime(new Date());
                updateOrInsertUserStatus(userStatusEntity);
                rt = true;
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("NotifyUserStatus ok.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("NotifyUserStatus failed.");
                }
            } else {
                commandResult.setCode(500);
                commandResult.setResult("User is not registered.");
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    public static void updateOrInsertUserStatus(UserStatusEntity userStatusEntity) {
        try {
            SqlSession sqlSession = MybatisUtil.getSqlSession();
            UserStatusDao userStatusDao = MybatisUtil.getSqlSession().getMapper(UserStatusDao.class);
            UserStatusEntity userStatus = userStatusDao.selectByUserId(userStatusEntity.getUserId());
            if (userStatus != null) {
                userStatusEntity.setId(userStatus.getId());
                userStatusDao.updateById(userStatusEntity);
            } else {
                userStatusDao.insert(userStatusEntity);
            }
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //哈工程对接，接收仿真系统推送的检测目标信息，例如雷达探测到的目标信息
    private void detectedInfo(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            DetectedInfoParam detectedInfoParam = JSON.toJavaObject((JSON) command.getParam(), DetectedInfoParam.class);
            boolean rt = true;
            rt = updateOrInsertSignLayer(detectedInfoParam.getTargets());
            if (rt) {
                commandResult.setCode(200);
                commandResult.setResult("Process detectedInfo ok.");
            } else {
                commandResult.setCode(500);
                commandResult.setResult("Process detectedInfo failed.");
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    public static boolean updateOrInsertSignLayer(List<SignLayerInfo> signLayerInfoList) {
        boolean rt = true;
        try {
            SqlSession sqlSession = MybatisUtil.getSqlSession();
            SignLayerDao signLayerDao = MybatisUtil.getSqlSession().getMapper(SignLayerDao.class);
            List<Point> position = new ArrayList<>();
            Gson gson = new Gson();
            for (int i = 0; i < signLayerInfoList.size(); i++) {
                SignLayerInfo signLayerInfo = signLayerInfoList.get(i);
                position.clear();
                SignLayerEntity signLayerEntity = signLayerDao.selectByName(signLayerInfo.getSignName());
                if (signLayerEntity != null) {
                    signLayerEntity.setSignType(signLayerInfo.getSignType());
                    signLayerEntity.setDescription(signLayerInfo.getDescription());
                    signLayerEntity.setCreatedTime(signLayerInfo.getCreatedTime());
                    signLayerEntity.setUpdateTime(signLayerInfo.getUpdateTime());
                    position.add(new Point(signLayerInfo.getLat(), signLayerInfo.getLng()));
                    signLayerEntity.setPosition(gson.toJson(position));
                    signLayerDao.updateById(signLayerEntity);
                } else {
                    signLayerEntity = new SignLayerEntity();
                    signLayerEntity.setSignType(signLayerInfo.getSignType());
                    signLayerEntity.setDescription(signLayerInfo.getDescription());
                    signLayerEntity.setCreatedTime(signLayerInfo.getCreatedTime());
                    signLayerEntity.setUpdateTime(signLayerInfo.getUpdateTime());
                    position.add(new Point(signLayerInfo.getLat(), signLayerInfo.getLng()));
                    signLayerEntity.setPosition(gson.toJson(position));
                    signLayerDao.insert(signLayerEntity);
                }
            }
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        } finally {
            MybatisUtil.close();
        }
        return rt;
    }

    //哈工程对接，接收仿真系统推送的AIS目标信息
    private void aisStatus(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            AisStatusParam aisStatusParam = JSON.toJavaObject((JSON) command.getParam(), AisStatusParam.class);
            boolean rt = true;
            rt = updateOrInsertAisInfo(aisStatusParam.getAisInfo());
            if (rt) {
                commandResult.setCode(200);
                commandResult.setResult("Process detectedInfo ok.");
            } else {
                commandResult.setCode(500);
                commandResult.setResult("Process detectedInfo failed.");
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    public static boolean updateOrInsertAisInfo(List<AisInfoEntity> aisInfoEntityList) {
        boolean rt = true;
        try {
            SqlSession sqlSession = MybatisUtil.getSqlSession();
            AisInfoDao aisInfoDao = MybatisUtil.getSqlSession().getMapper(AisInfoDao.class);
            for (int i = 0; i < aisInfoEntityList.size(); i++) {
                AisInfoEntity aisInfoEntity = aisInfoEntityList.get(i);
                AisInfoEntity entity = aisInfoDao.selectByMMSI(aisInfoEntity.getMmsiId());
                if (entity != null) {
                    aisInfoEntity.setId(entity.getId());
                    aisInfoDao.updateById(entity);
                } else {
                    aisInfoDao.insert(aisInfoEntity);
                }
            }
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        } finally {
            MybatisUtil.close();
        }
        return rt;
    }


    //登录系统，用于验证用户身份，实现任务下发到指定的人员
    private void login(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            LoginParam loginParam = JSON.toJavaObject((JSON) command.getParam(), LoginParam.class);
            boolean rt = false;
            //根据userId验证用户的合法性
            UserDao userDao = MybatisUtil.getSqlSession().getMapper(UserDao.class);
            UserEntity userEntity = userDao.selectById(loginParam.getUserId());
            if (userEntity != null) {
                if (userEntity.getUsername().equals(loginParam.getName()) && userEntity.getPassword().toUpperCase().equals(Convert.MD5EncodeToHex(loginParam.getPwd()).toUpperCase())) {
                    //将该session添加key，用作标识，下发任务时，根据此标识进行下发
                    session.setAttribute(loginKey, loginParam);
                    rt = true;
                }
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("Login ok.");
                } else {
                    if (session.containsAttribute(loginKey)) {
                        session.removeAttribute(loginKey);
                    }
                    commandResult.setCode(500);
                    commandResult.setResult("Login failed.Username or password is not right.");
                }
            } else {
                commandResult.setCode(500);
                commandResult.setResult("User is not registered.");
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //登录上级系统，用于实现任务下发到指定的地面站
    private void loginParent(IoSession session, Command command) {
        try {
//            CommandResult commandResult = new CommandResult();
//            IoBuffer buffer = IoBuffer.allocate(200);
//            LoginParam loginParam = JSON.toJavaObject((JSON) command.getParam(), LoginParam.class);
//            boolean rt = false;
//            //根据userId验证用户的合法性
//            UserDao userDao = MybatisUtil.getSqlSession().getMapper(UserDao.class);
//            UserEntity userEntity = userDao.selectById(loginParam.getUserId());
//            if (userEntity != null) {
//                if (userEntity.getUsername().equals(loginParam.getName()) && userEntity.getPassword().equals(loginParam.getPwd())) {
//                    //将该session添加key，用作标识，下发任务时，根据此标识进行下发
//                    session.setAttribute(loginKey, loginParam);
//                    rt = true;
//                }
//                if (rt) {
//                    commandResult.setCode(200);
//                    commandResult.setResult("Login ok.");
//                } else {
//                    commandResult.setCode(500);
//                    commandResult.setResult("Login failed.Username or password is not right.");
//                }
//            } else {
//                commandResult.setCode(500);
//                commandResult.setResult("User is not registered.");
//            }
//            Gson gson = new Gson();
//            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
//            buffer.flip();
//            session.write(buffer);
//            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //下发任务到指定的移动端或者地面站
    private void sendTask(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            buffer.setAutoExpand(true);
            Gson gson = new Gson();
            SendTaskInfo sendTaskInfo = JSON.toJavaObject((JSON) command.getParam(), SendTaskInfo.class);
            boolean rt = false;
            //根据userId验证用户的合法性
            if (sendTaskInfo.getUserId() == 0) {
                //若userId为0，表示进行广播
                //根据userId查找对应的session，将该消息发送到指定的客户端（移动端或地面站）
                Collection<IoSession> sessions = session.getService().getManagedSessions().values();
                long sendCount = 0;
                switch (sendTaskInfo.getClientType()) {
                    case ClientType.PC:
                        //下发到地面站
                        for (IoSession s : sessions) {
                            if (s.containsAttribute(loginKey)) {
                                LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                if (loginParam.getClientType().equals(ClientType.PC)) {
                                    buffer.clear();
                                    buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                    buffer.flip();
                                    s.write(buffer);
                                    ++sendCount;
                                    Thread.sleep(10);
                                }
                            }
                        }
                        break;
                    case ClientType.MOBILE:
                        //下发到移动端
                        for (IoSession s : sessions) {
                            if (s.containsAttribute(loginKey)) {
                                LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                if (loginParam.getClientType().equals(ClientType.ANDROID) || loginParam.getClientType().equals(ClientType.IOS)) {
                                    buffer.clear();
                                    buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                    buffer.flip();
                                    s.write(buffer);
                                    ++sendCount;
                                    Thread.sleep(10);
                                }
                            }
                        }
                        break;
                    case ClientType.ALL:
                    default:
                        //下发到全部客户端
                        for (IoSession s : sessions) {
                            if (s.containsAttribute(loginKey)) {
                                buffer.clear();
                                buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                buffer.flip();
                                s.write(buffer);
                                ++sendCount;
                                Thread.sleep(10);
                            }
                        }
                        break;
                }
                log.info("Try to send task to client. SendCount:" + sendCount);
                rt = true;
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("SendTask ok.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("SendTask failed.");
                }
            } else {
                UserDao userDao = MybatisUtil.getSqlSession().getMapper(UserDao.class);
                UserEntity userEntity = userDao.selectById(sendTaskInfo.getUserId());
                if (userEntity != null) {
                    //根据userId查找对应的session，将该消息发送到指定的客户端（移动端或地面站）
                    Collection<IoSession> sessions = session.getService().getManagedSessions().values();
                    long sendCount = 0;
                    switch (sendTaskInfo.getClientType()) {
                        case ClientType.PC:
                            //下发到地面站
                            for (IoSession s : sessions) {
                                if (s.containsAttribute(loginKey)) {
                                    LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                    if ((loginParam.getUserId() == sendTaskInfo.getUserId()) && loginParam.getClientType().equals(ClientType.PC)) {
                                        buffer.clear();
                                        buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                        buffer.flip();
                                        s.write(buffer);
                                        ++sendCount;
                                        Thread.sleep(10);
                                    }
                                }
                            }

                            break;
                        case ClientType.MOBILE:
                            //下发到移动端
                            for (IoSession s : sessions) {
                                if (s.containsAttribute(loginKey)) {
                                    LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                    if ((loginParam.getUserId() == sendTaskInfo.getUserId()) && (loginParam.getClientType().equals(ClientType.ANDROID) || loginParam.getClientType().equals(ClientType.IOS))) {
                                        buffer.clear();
                                        buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                        buffer.flip();
                                        s.write(buffer);
                                        ++sendCount;
                                        Thread.sleep(10);
                                    }
                                }
                            }
                            break;
                        case ClientType.ALL:
                        default:
                            //下发到全部客户端
                            for (IoSession s : sessions) {
                                if (s.containsAttribute(loginKey)) {
                                    LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                    if (loginParam.getUserId() == sendTaskInfo.getUserId()) {
                                        buffer.clear();
                                        buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                        buffer.flip();
                                        s.write(buffer);
                                        ++sendCount;
                                        Thread.sleep(10);
                                    }
                                }
                            }
                            break;
                    }
                    log.info("Try to send task to client. SendCount:" + sendCount);
                    rt = true;
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("SendTask ok.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("SendTask failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("User is not registered.");
                }
            }
            buffer.clear();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //下发指令到指定的移动端或者地面站
    private void sendInstruction(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            buffer.setAutoExpand(true);
            Gson gson = new Gson();
            SendInstructionInfo sendInstructionInfo = JSON.toJavaObject((JSON) command.getParam(), SendInstructionInfo.class);
            List<Integer> userIdList = new ArrayList<>();
            if (sendInstructionInfo.getRelativeUser() != null && !sendInstructionInfo.getRelativeUser().isEmpty()) {
                List<UserSimpleInfo> users = JSON.parseArray(sendInstructionInfo.getRelativeUser(), UserSimpleInfo.class);
                for (int i = 0; i < users.size(); i++) {
                    userIdList.add(users.get(i).getId());
                }
            }
            boolean rt = false;
            //根据userId验证用户的合法性
            if (sendInstructionInfo.getUserId() == 0) {
                //若userId为0，表示进行广播
                //根据userId查找对应的session，将该消息发送到指定的客户端（移动端或地面站）
                Collection<IoSession> sessions = session.getService().getManagedSessions().values();
                long sendCount = 0;
                switch (sendInstructionInfo.getClientType()) {
                    case ClientType.PC:
                        //下发到地面站
                        for (IoSession s : sessions) {
                            if (s.containsAttribute(loginKey)) {
                                LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                if (loginParam.getClientType().equals(ClientType.PC)) {
                                    buffer.clear();
                                    buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                    buffer.flip();
                                    s.write(buffer);
                                    ++sendCount;
                                    Thread.sleep(10);
                                }
                            }
                        }
                        break;
                    case ClientType.MOBILE:
                        //下发到移动端
                        for (IoSession s : sessions) {
                            if (s.containsAttribute(loginKey)) {
                                LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                if (loginParam.getClientType().equals(ClientType.ANDROID) || loginParam.getClientType().equals(ClientType.IOS)) {
                                    buffer.clear();
                                    buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                    buffer.flip();
                                    s.write(buffer);
                                    ++sendCount;
                                    Thread.sleep(10);
                                }
                            }
                        }
                        break;
                    case ClientType.ALL:
                    default:
                        //下发到全部客户端
                        for (IoSession s : sessions) {
                            if (s.containsAttribute(loginKey)) {
                                buffer.clear();
                                buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                buffer.flip();
                                s.write(buffer);
                                ++sendCount;
                                Thread.sleep(10);
                            }
                        }
                        break;
                }
                log.info("Try to send instruction to client. SendCount:" + sendCount);
                rt = true;
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("SendInstruction ok.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("SendInstruction failed.");
                }
            } else {
                UserDao userDao = MybatisUtil.getSqlSession().getMapper(UserDao.class);
                UserEntity userEntity = userDao.selectById(sendInstructionInfo.getUserId());
                if (userEntity != null) {
                    if (userIdList.size() > 0) {
                        //根据userId查找对应的session，将该消息发送到指定的客户端（移动端或地面站）
                        Collection<IoSession> sessions = session.getService().getManagedSessions().values();
                        long sendCount = 0;
                        switch (sendInstructionInfo.getClientType()) {
                            case ClientType.PC:
                                //下发到地面站
                                for (IoSession s : sessions) {
                                    if (s.containsAttribute(loginKey)) {
                                        LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                        if ((userIdList.contains(loginParam.getUserId())) && loginParam.getClientType().equals(ClientType.PC)) {
                                            buffer.clear();
                                            buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                            buffer.flip();
                                            s.write(buffer);
                                            ++sendCount;
                                            Thread.sleep(10);
                                        }
                                    }
                                }

                                break;
                            case ClientType.MOBILE:
                                //下发到移动端
                                for (IoSession s : sessions) {
                                    if (s.containsAttribute(loginKey)) {
                                        LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                        if ((userIdList.contains(loginParam.getUserId())) && (loginParam.getClientType().equals(ClientType.ANDROID) || loginParam.getClientType().equals(ClientType.IOS))) {
                                            buffer.clear();
                                            buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                            buffer.flip();
                                            s.write(buffer);
                                            ++sendCount;
                                            Thread.sleep(10);
                                        }
                                    }
                                }
                                break;
                            case ClientType.ALL:
                            default:
                                //下发到全部客户端
                                for (IoSession s : sessions) {
                                    if (s.containsAttribute(loginKey)) {
                                        LoginParam loginParam = (LoginParam) s.getAttribute(loginKey);
                                        if (userIdList.contains(loginParam.getUserId())) {
                                            buffer.clear();
                                            buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                                            buffer.flip();
                                            s.write(buffer);
                                            ++sendCount;
                                            Thread.sleep(10);
                                        }
                                    }
                                }
                                break;
                        }
                        log.info("Try to send instruction to client. SendCount:" + sendCount);
                        rt = true;
                    } else {
                        rt = false;
                    }
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("SendInstruction ok.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("SendInstruction failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("User is not registered.");
                }
            }
            buffer.clear();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    private void manualControl(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            ManualControlParam manualControlParam = JSON.toJavaObject((JSON) command.getParam(), ManualControlParam.class);
            boolean rt = false;
            //根据第一个设备的controlType来判断，若是移动端控制，则下发给APP进行远程控制，否则给droneService进行直接控制
            DroneDao droneDao = MybatisUtil.getSqlSession().getMapper(DroneDao.class);
            int deviceId = manualControlParam.getDeviceId();
            DroneEntity droneEntity = droneDao.selectByDeviceId(deviceId);
            if (droneEntity.getControlType() == ControlType.CONTROL_MOBILE) {
                rt = appControl(session, command, deviceId);
                if (rt) {
                    commandResult.setCode(200);
                    commandResult.setResult("ManualControl success.Try to send to APP.");
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("ManualControl failed.");
                }
            } else {
                if (droneService != null) {
                    rt = droneService.manualControl(manualControlParam.getDeviceId(), manualControlParam.getDeviceType(),
                            manualControlParam.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, manualControlParam.getX(), manualControlParam.getY(), manualControlParam.getZ(), manualControlParam.getR());
                    if (rt) {
                        commandResult.setCode(200);
                        commandResult.setResult("ManualControl success.");
                    } else {
                        commandResult.setCode(500);
                        commandResult.setResult("ManualControl failed.");
                    }
                } else {
                    commandResult.setCode(500);
                    commandResult.setResult("Drone service is not alive.");
                }
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribeVisionLand(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            SubscribeVisionLandParam subscribeVisionLandParam = JSON.toJavaObject((JSON) command.getParam(), SubscribeVisionLandParam.class);
            boolean rt = false;
            //将该session添加key，用作标识下发视觉自主降落指令
            session.setAttribute(visionLandKey, subscribeVisionLandParam);
            rt = true;
            if (rt) {
                commandResult.setCode(200);
                commandResult.setResult("Subscribe VisionLand ok.");
            } else {
                if (session.containsAttribute(visionLandKey)) {
                    session.removeAttribute(visionLandKey);
                }
                commandResult.setCode(500);
                commandResult.setResult("Subscribe VisionLand failed.");
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    private void visionLand(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            buffer.setAutoExpand(true);
            Gson gson = new Gson();
            VisionLandParam visionLandParam = JSON.toJavaObject((JSON) command.getParam(), VisionLandParam.class);
            boolean rt = false;
            //根据sysId查找对应的session，将该消息发送到指定的客户端（移动端或地面站）
            if (commandService != null && commandService.getTcpServer() != null) {
                Collection<IoSession> sessions = commandService.getTcpServer().getAcceptor().getManagedSessions().values();
                long sendCount = 0;
                //下发到全部客户端
                for (IoSession s : sessions) {
                    if (s.containsAttribute(visionLandKey)) {
                        SubscribeVisionLandParam subscribeVisionLandParam = (SubscribeVisionLandParam) s.getAttribute(visionLandKey);
                        if (subscribeVisionLandParam.getSysId() == visionLandParam.getMove().getSysId()) {
                            buffer.clear();
                            buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                            buffer.flip();
                            s.write(buffer);
                            ++sendCount;
                            Thread.sleep(10);
                        }
                    }
                }
                log.info("Try to send VisionLand to client. SendCount:" + sendCount);
                rt = true;
            }
            if (rt) {
                commandResult.setCode(200);
                commandResult.setResult("SendVisionLand ok.");
            } else {
                commandResult.setCode(500);
                commandResult.setResult("SendVisionLand failed.");
            }
            buffer.clear();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }


    private void subscribeAppControl(IoSession session, Command command) {
        try {
            CommandResult commandResult = new CommandResult();
            IoBuffer buffer = IoBuffer.allocate(200);
            SubscribeAppControlParam subscribeAppControlParam = JSON.toJavaObject((JSON) command.getParam(), SubscribeAppControlParam.class);
            boolean rt = false;
            //将该session添加key，用作标识下发指令
            session.setAttribute(appControlKey, subscribeAppControlParam);
            rt = true;
            if (rt) {
                commandResult.setCode(200);
                commandResult.setResult("Subscribe AppControl ok.");
            } else {
                if (session.containsAttribute(appControlKey)) {
                    session.removeAttribute(appControlKey);
                }
                commandResult.setCode(500);
                commandResult.setResult("Subscribe AppControl failed.");
            }
            Gson gson = new Gson();
            buffer.putString(gson.toJson(commandResult) + "\r\n", StandardCharsets.UTF_8.newEncoder());
            buffer.flip();
            session.write(buffer);
            log.info(commandResult.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }
    }

    //将无人机控制指令转发给APP
    private boolean appControl(IoSession session, Command command, int deviceId) {
        boolean rt = false;
        try {
            IoBuffer buffer = IoBuffer.allocate(200);
            buffer.setAutoExpand(true);
            Gson gson = new Gson();
            //根据deviceId查找对应的session，将该消息发送到指定的客户端
            if (commandService != null && commandService.getTcpServer() != null) {
                Collection<IoSession> sessions = commandService.getTcpServer().getAcceptor().getManagedSessions().values();
                long sendCount = 0;
                //下发到全部客户端
                for (IoSession s : sessions) {
                    if (s.containsAttribute(appControlKey)) {
                        SubscribeAppControlParam subscribeAppControlParam = (SubscribeAppControlParam) s.getAttribute(appControlKey);
                        if (subscribeAppControlParam.getDeviceId() == deviceId) {
                            buffer.clear();
                            buffer.putString(gson.toJson(command) + "\r\n", StandardCharsets.UTF_8.newEncoder());
                            buffer.flip();
                            s.write(buffer);
                            ++sendCount;
                            Thread.sleep(10);
                        }
                    }
                }
                log.info("Try to send AppControl to client. SendCount:" + sendCount);
                rt = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        } finally {
            MybatisUtil.close();
        }
        return rt;
    }
}

