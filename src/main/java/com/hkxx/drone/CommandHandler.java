package com.hkxx.drone;

import com.MAVLink.enums.MAV_COMPONENT;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hkxx.drone.db.MybatisUtil;
import com.hkxx.drone.db.dao.DroneDao;
import com.hkxx.drone.db.dao.FlightpathDao;
import com.hkxx.drone.db.dao.TaskDao;
import com.hkxx.drone.db.entity.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CommandHandler extends IoHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(CommandHandler.class);
    int tsWait = 5000; // 等待信号量事件通知的时间，单位毫秒

    DroneService droneService = null;


    public DroneService getDroneService() {
        return droneService;
    }

    public void setDroneService(DroneService droneService) {
        this.droneService = droneService;
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
            if (droneService != null) {
                MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
                boolean rt = false;
                //根据task_id查询任务类型等参数，执行任务
                TaskDao taskDao = (TaskDao) MybatisUtil.getSqlSession().getMapper(TaskDao.class);
                TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
                DeviceInfo deviceInfo = null;
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
            if (droneService != null) {
                MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
                boolean rt = false;
                //根据task_id查询任务类型等参数，执行任务
                TaskDao taskDao = (TaskDao) MybatisUtil.getSqlSession().getMapper(TaskDao.class);
                TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
                DeviceInfo deviceInfo = null;
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
            if (droneService != null) {
                MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
                boolean rt = false;
                //根据task_id查询任务类型等参数，执行任务
                TaskDao taskDao = (TaskDao) MybatisUtil.getSqlSession().getMapper(TaskDao.class);
                TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
                DeviceInfo deviceInfo = null;
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
            if (droneService != null) {
                MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
                boolean rt = false;
                //根据task_id查询任务类型等参数，执行任务
                TaskDao taskDao = (TaskDao) MybatisUtil.getSqlSession().getMapper(TaskDao.class);
                TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
                DeviceInfo deviceInfo = null;
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
            if (droneService != null) {
                MissionParam missionParam = JSON.toJavaObject((JSON) command.getParam(), MissionParam.class);
                boolean rt = false;
                //根据task_id查询任务类型等参数，执行任务
                TaskDao taskDao = (TaskDao) MybatisUtil.getSqlSession().getMapper(TaskDao.class);
                TaskEntity taskEntity = taskDao.selectById(missionParam.getTaskId());
                DeviceInfo deviceInfo = null;
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
            if (droneService != null) {
                GotoPointParam gotoPointParam = JSON.toJavaObject((JSON) command.getParam(), GotoPointParam.class);
                boolean rt = droneService.gotoPosition(gotoPointParam.getDeviceId(),
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
}

