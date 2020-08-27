package com.hkxx.drone;

import com.MAVLink.enums.MAV_COMPONENT;
import com.alibaba.fastjson.JSON;
import com.hkxx.drone.db.MybatisUtil;
import com.hkxx.drone.db.dao.DroneDao;
import com.hkxx.drone.db.dao.FlightpathDao;
import com.hkxx.drone.db.dao.PortDao;
import com.hkxx.drone.db.dao.SwarmDao;
import com.hkxx.drone.db.entity.*;
import com.hkxx.drone.worker.LinkhubProfileManager;
import com.hkxx.drone.worker.Worker;
import com.hkxx.drone.worker.template.*;
import com.hkxx.util.DeviceLinkAddress;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * 无人机控制服务类，提供无人机的连接、控制等功能
 */
public class DroneService implements DroneControl.DeviceStateChanged {

    private static Logger log = LoggerFactory.getLogger(DroneService.class);

    Thread tCheckDroneState = null;
    boolean isStop = false;
    int tsCheckTime = 30 * 1000; //检测无人机控制对象的周期，单位毫秒，默认30秒
    //无人机控制对象哈希表，无人机的序号ID为key
    private HashMap<Integer, DroneControl> droneMap = new HashMap<>();
    private HashMap<Integer, SwarmState> swarmStateMap = new HashMap<>();
    private Object synObject = new Object();

    public DroneService() {

    }

    public int getTsCheckTime() {
        return tsCheckTime;
    }

    public void setTsCheckTime(int tsCheckTime) {
        this.tsCheckTime = tsCheckTime;
    }

    public void start() {
        try {
            //扫描数据库，判断drone表是否有变化，若有变化则更新无人机控制表droneMap，更新linkhub配置文件通知linkhub服务刷新
            isStop = false;
            tCheckDroneState = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("DroneService is started.");
                    boolean isExist = false;
                    List<Integer> removeList = new ArrayList<>();
                    while (!isStop) {
                        SqlSession sqlSession = null;
                        try {
                            sqlSession = MybatisUtil.getSqlSession();
                            DroneDao droneDao = sqlSession.getMapper(DroneDao.class);
                            List<DroneEntity> droneList = droneDao.selectByControlType(ControlType.CONTROL_CLUSTER);
                            DroneEntity drone = null;
                            DroneControl droneControl = null;
                            //加载新的无人机连接对象
                            for (int i = 0; i < droneList.size(); i++) {
                                drone = droneList.get(i);
                                droneControl = new DroneControl();
                                DeviceLinkAddress linkAddress = DeviceLinkAddress.parse(drone.getLinkaddress());
                                if (linkAddress.host.equals(DeviceLinkAddress.HOST_LOCALHOST_IP) || linkAddress.port == DeviceLinkAddress.PORT_TOGEN) {
                                    log.info("Device is using default localhost and port,please check if it is right.Drone's deviceId:" + drone.getDeviceId() + " Name:" + drone.getName());
                                }
                                droneControl.setDevIP(linkAddress.host);
                                droneControl.setDevPort(linkAddress.port);
                                droneControl.setLinkhubIP(Config.linkhubIP);
                                droneControl.setSessionTimeout(Config.sessionTimeout);
                                //检测该无人机是否分配linkhub端口，若没有，则分配端口
                                droneControl.setLinkhubPort(genLinkhubPort(drone.getDeviceId()));
                                droneControl.setDeviceId(drone.getDeviceId()); //保存设备ID
                                droneControl.setDeviceStateChangedListener(DroneService.this);
                                if (droneMap.containsKey(drone.getDeviceId())) {
                                    //检测无人机的配置是否有变化，若偶变化则更新linkhub配置文件，通知linkhub更新服务
                                    if (!droneMap.get(drone.getDeviceId()).isEqual(droneControl)) {
                                        //更新linkhub配置文件
                                        updateLinkhubProfile(drone.getDeviceId(), drone.getName(), droneControl.getDevIP(), droneControl.getDevPort(), droneControl.getLinkhubPort());
                                        droneMap.put(drone.getDeviceId(), droneControl);
                                        droneControl.stopConnection();//停止linkhub连接
                                        droneControl.startConnection();//开启linkhub连接
                                    }
                                } else {
                                    //若不存在，则添加无人机控制对象至droneMap，建立linkhub连接
                                    //更新linkhub配置文件
                                    updateLinkhubProfile(drone.getDeviceId(), drone.getName(), droneControl.getDevIP(), droneControl.getDevPort(), droneControl.getLinkhubPort());
                                    droneMap.put(drone.getDeviceId(), droneControl);
                                    droneControl.startConnection();//开启linkhub连接
                                }
                            }
                            //比对当前无人机列表，清理已经删掉的无人机连接对象
                            removeList.clear();
                            for (Integer droneId : droneMap.keySet()) {
                                isExist = false;
                                for (int i = 0; i < droneList.size(); i++) {
                                    if (droneId == droneList.get(i).getDeviceId()) {
                                        isExist = true;
                                        break;
                                    }
                                }
                                if (!isExist) {
                                    //需要清理掉当前的无人机连接对象
                                    removeList.add(droneId);
                                }
                            }
                            //清理无人机连接对象
                            for (Integer droneId : removeList) {
                                DroneControl clearDroneControl = droneMap.get(droneId);
                                clearDroneControl.stopConnection();
                                clearDroneControl = null;
                                droneMap.remove(droneId);
                            }
                            //清理资源，释放内存，提醒虚拟机回收内存
                            System.gc();
                            //强制调用已经失去引用对象的finalize方法
                            System.runFinalization();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            MybatisUtil.close();
                        }
                        try {
                            Thread.sleep(tsCheckTime);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    log.info("DroneService is stopped.");
                }
            });
            tCheckDroneState.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isStop = true;
    }

    private int genLinkhubPort(int droneId) {
        int port = 0;
        try {
            SqlSession sqlSession = MybatisUtil.getSqlSession();
            PortDao portDao = sqlSession.getMapper(PortDao.class);
            List<PortEntity> ports = portDao.selectByDeviceId(droneId, 0, Constants.CATEGORY_DRONE_LINKHUB, 0, 1);
//            ports = portDao.selectList(null);
            if (ports.size() > 0) {
                port = ports.get(0).getValue();
            } else {
                //不存在，则分配端口
                List<PortEntity> newPorts = portDao.selectByDeviceId(null, 1, Constants.CATEGORY_NONE, 0, 1);
                if (newPorts.size() > 0) {
                    PortEntity newPort = newPorts.get(0);
                    newPort.setAvailable(0);
                    newPort.setCategory(Constants.CATEGORY_DRONE_LINKHUB);
                    newPort.setDeviceId(droneId);
                    portDao.updateById(newPort);
                    sqlSession.commit();
                    port = newPort.getValue();
                } else {
                    log.info("There is not enough port to use.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            port = 0;
        } finally {
            MybatisUtil.close();
        }
        return port;
    }

    private void updateLinkhubProfile(int droneId, String droneName, String deviceHost, int devicePort, int linkhubPort) {
        try {
            String droneProfileName = Worker.makeDroneProfileName(droneId);
            LinkhubProfile profile = null;

            if (Config.deviceUseRTCP) {
                profile = new DroneOutIn2Profile(droneProfileName,
                        droneName, Calendar.getInstance().getTime()
                        .toGMTString(), "0", "device", deviceHost,
                        String.valueOf(devicePort), "1", "client", String.valueOf(linkhubPort),
                        Config.mavlogWorkspace,
                        Worker.makeDroneMavlogPrefix(droneId));
            } else {
                profile = new DroneOutIn1Profile(droneProfileName,
                        droneName, Calendar.getInstance().getTime()
                        .toGMTString(), "0", "device", deviceHost,
                        String.valueOf(devicePort), "1", "client", String.valueOf(linkhubPort),
                        Config.mavlogWorkspace,
                        Worker.makeDroneMavlogPrefix(droneId));
            }
            if (!LinkhubProfileManager.updateProfile(profile)) {
                LinkhubProfileManager.deleteProfile(profile.name);
            }
            Thread.sleep(4000); // wait for linkhub update
            // profile
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean doCreateSwarmFlight(int swarmId) {
        try {
            log.info("doCreateSwarmFlight:" + swarmId);
            String profileName = Worker.makeSwarmFlightProfileName(swarmId);
            SqlSession session = MybatisUtil.getSqlSession();
            List<SwarmDevicePort> swarmDevicePorts = null;

            try {
                SwarmDao swarmDao = session.getMapper(SwarmDao.class);
                swarmDevicePorts = swarmDao.selectSwarmDevicePorts(swarmId);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                MybatisUtil.close();
            }

            if (swarmDevicePorts != null && swarmDevicePorts.size() > 1) {
                log.info("doCreateSwarmFlight.swarmId:" + swarmId
                        + ", swarmDronePorts.size:" + swarmDevicePorts.size());
                List<SwarmFlightRLIItem> rliItems = new ArrayList<>();
                List<SwarmFlightRRItem> rrItems = new ArrayList<>();
                for (SwarmDevicePort swarmDevicePort : swarmDevicePorts) {
                    SwarmFlightRLIItem item = new SwarmFlightRLIItem(swarmDevicePort
                            .getDeviceId().toString(), swarmDevicePort.getDeviceId()
                            .toString(), Config.linkhubIP, swarmDevicePort
                            .getClientPort().toString());
                    rliItems.add(item);

                    List<SwarmFlightRRItemArrayItem> rrItemArrayItems = new ArrayList<>();
                    String droneId = null;
                    for (int i = 0; i < swarmDevicePorts.size(); i++) {
                        SwarmDevicePort port = swarmDevicePorts.get(i);
                        if (port.getClientPort().intValue() == swarmDevicePort
                                .getClientPort().intValue()) {
                            droneId = swarmDevicePort.getDeviceId().toString();
                        } else {
                            SwarmFlightRRItemArrayItem rrItemArrayItem = new SwarmFlightRRItemArrayItem(
                                    port.getDeviceId().toString());
                            rrItemArrayItems.add(rrItemArrayItem);
                        }
                    }
                    if (droneId != null) {
                        SwarmFlightRRItem rrItem = new SwarmFlightRRItem(droneId,
                                rrItemArrayItems);
                        rrItems.add(rrItem);
                    }
                }
                SwarmFlight swarmFlight = new SwarmFlight(profileName, profileName,
                        Calendar.getInstance().getTime().toGMTString(), rliItems,
                        rrItems);
                if (!LinkhubProfileManager.updateProfile(swarmFlight)) {
                    LinkhubProfileManager.deleteProfile(profileName);
                    log.warn("doCreateSwarmFlight.updateProfile failed!");
                    return false;
                }
                log.info("doCreateSwarmFlight.updateProfile succ!");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private static boolean doDeleteSwarmFlight(int swarmId) {
        log.info("doDeleteSwarmFlight:" + swarmId);
        String profileName = Worker.makeSwarmFlightProfileName(swarmId);
        LinkhubProfileManager.deleteProfile(profileName);
        return true;
    }

    //上传航点飞行任务到指定的无人机
    public boolean uploadMission(int deviceId, PointFlyContent content) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.uploadMission(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //给指定的无人机，发送开始起飞指令，执行航点飞行任务
    public boolean startMission(int deviceId, int targetSystemId, int targetComponentId, int missionCount) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.startMission(targetSystemId, targetComponentId, missionCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //停止执行航点飞行任务
    public boolean stopMission(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.stopMission(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //暂停执行航点飞行任务
    public boolean pauseMission(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.pauseMission(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //恢复执行航点飞行任务
    public boolean resumeMission(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.resumeMission(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //返航
    public boolean goHome(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.goHome(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //上传集群任务到指定的无人机
    public boolean uploadClusterMission(ClusterFlyContent content) {
        boolean rt = false;
        try {
            //找到长机，将航点mission上传至长机，其他的僚机采用hold模式
            if (content != null) {
                //生成集群的linkhub配置文件
                doCreateSwarmFlight(content.getSwarmId());
                //第一个为长机，后面的为僚机
                List<DeviceInfo> deviceInfoList = content.getDevice();
                DeviceInfo deviceInfo = null;
                int deviceId = 0;
                int sysId = 0;
                int deviceType = 0;
                int collaborationValue = 0;
                //加入到swarmState表中
                SwarmState swarmState = new SwarmState();
                swarmState.setSwarmId(content.getSwarmId());
                for (int i = 0; i < deviceInfoList.size(); i++) {
                    //添加deviceState到swarmState中
                    deviceInfo = deviceInfoList.get(i);
                    DeviceState deviceState = new DeviceState();
                    deviceState.setDeviceId(deviceInfo.getDeviceId());
                    deviceState.setDeviceType(deviceInfo.getDeviceType());
                    deviceState.setSysId(deviceInfo.getSysId());
                    deviceState.setTaskDone(false);
                    swarmState.getDeviceStateList().add(deviceState);
                }
                //更新或加入到swarmState状态表
                synchronized (synObject) {
                    swarmStateMap.put(content.getSwarmId(), swarmState);
                }
                //长机在前，僚机依次在后，填入sysId，最多4架飞机
                for (int i = 0; i < deviceInfoList.size(); i++) {
                    if (i < 4) {
                        collaborationValue += (deviceInfoList.get(i).getSysId() & 0xff) << (8 * (3 - i));
                    } else {
                        break;
                    }
                }
                for (int i = 0; i < deviceInfoList.size(); i++) {
                    deviceInfo = deviceInfoList.get(i);
                    deviceId = deviceInfo.getDeviceId();
                    sysId = deviceInfo.getSysId();
                    deviceType = deviceInfo.getDeviceType();
                    if (droneMap.containsKey(deviceId)) {
                        DroneControl droneControl = droneMap.get(deviceId);
                        //发送阵形
                        rt = droneControl.sendCollaboration(sysId, MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, content.getCollaboration());
                        if (!rt) {
                            return rt;
                        }
                        //发送阵形编排值
                        rt = droneControl.setCollaborationValue(sysId, MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, collaborationValue);
                        if (!rt) {
                            return rt;
                        }
                        //发送mission至长机
                        if (i == 0) {
                            PointFlyContent pointFlyContent = new PointFlyContent();
                            pointFlyContent.setDeviceId(deviceId);
                            pointFlyContent.setSpeed(content.getSpeed());
                            pointFlyContent.setHeight(content.getHeight());
                            pointFlyContent.setSysId(sysId);
                            pointFlyContent.setDeviceType(deviceType);
                            pointFlyContent.setFlightContent(content.getFlightContent());
                            rt = droneControl.uploadMission(pointFlyContent);
                            if (!rt) {
                                return rt;
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //开始执行集群飞行任务
    public boolean startClusterMission(MissionParam missionParam) {
        boolean rt = false;
        try {
            FlightpathDao flightpathDao = (FlightpathDao) MybatisUtil.getSqlSession().getMapper(FlightpathDao.class);
            List<FlightpathEntity> flightpathEntities = flightpathDao.selectByTaskId(missionParam.getTaskId());
            List<Waypoint> content = JSON.parseArray(flightpathEntities.get(0).getContent(), Waypoint.class);
            int missionCount = content.size();
            //给长机发送startMission指令，给僚机发送hold指令
            List<DeviceInfo> deviceInfoList = missionParam.getDevice();
            DeviceInfo deviceInfo = null;
            DeviceInfo headDevice = null;
            //依次给所有飞机发送takeoff指令和hold指令
            for (int i = 0; i < deviceInfoList.size(); i++) {
                deviceInfo = deviceInfoList.get(i);
                if (i == 0) {
                    headDevice = deviceInfo; //保存长机信息
                } else {
                    if (droneMap.containsKey(deviceInfo.getDeviceId())) {
                        DroneControl droneControl = droneMap.get(deviceInfo.getDeviceId());
                        rt = droneControl.takeoff(deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, 1);
//                    if (!rt) {
//                        return rt;
//                    }
                        rt = droneControl.setHold(deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
//                    if (!rt) {
//                        return rt;
//                    }
                    }
                }
            }
            if (headDevice != null) {
                //第一个为长机，发送startmission指令
                if (droneMap.containsKey(headDevice.getDeviceId())) {
                    DroneControl droneControl = droneMap.get(headDevice.getDeviceId());
                    rt = droneControl.startMission(headDevice.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1, missionCount);
                }
            } else {
                rt = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        } finally {
            MybatisUtil.close();
        }
        return rt;
    }

    //停止执行集群飞行任务
    public boolean stopClusterMission(MissionParam missionParam) {
        boolean rt = false;
        try {
            //给长机发送stopMission指令，给僚机发送hold指令
            List<DeviceInfo> deviceInfoList = missionParam.getDevice();
            DeviceInfo deviceInfo = null;
            boolean isOk = false;
            for (int i = 0; i < deviceInfoList.size(); i++) {
                deviceInfo = deviceInfoList.get(i);
                if (i == 0) {
                    //第一个为长机
                    if (droneMap.containsKey(deviceInfo.getDeviceId())) {
                        DroneControl droneControl = droneMap.get(deviceInfo.getDeviceId());
                        rt = droneControl.stopMission(deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    }
                } else {
                    //僚机发送hold指令
                    if (droneMap.containsKey(deviceInfo.getDeviceId())) {
                        DroneControl droneControl = droneMap.get(deviceInfo.getDeviceId());
                        rt = droneControl.setHold(deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //暂停执行集群飞行任务
    public boolean pauseClusterMission(MissionParam missionParam) {
        boolean rt = false;
        try {
            //给长机发送pauseMission指令，给僚机发送hold指令
            List<DeviceInfo> deviceInfoList = missionParam.getDevice();
            DeviceInfo deviceInfo = null;
            boolean isOk = false;
            for (int i = 0; i < deviceInfoList.size(); i++) {
                deviceInfo = deviceInfoList.get(i);
                if (i == 0) {
                    //第一个为长机
                    if (droneMap.containsKey(deviceInfo.getDeviceId())) {
                        DroneControl droneControl = droneMap.get(deviceInfo.getDeviceId());
                        rt = droneControl.pauseMission(deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    }
                } else {
                    //僚机发送hold指令
                    if (droneMap.containsKey(deviceInfo.getDeviceId())) {
                        DroneControl droneControl = droneMap.get(deviceInfo.getDeviceId());
                        rt = droneControl.setHold(deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //恢复执行集群飞行任务
    public boolean resumeClusterMission(MissionParam missionParam) {
        boolean rt = false;
        try {
            //给长机发送resumeMission指令，给僚机发送hold指令
            List<DeviceInfo> deviceInfoList = missionParam.getDevice();
            DeviceInfo deviceInfo = null;
            boolean isOk = false;
            for (int i = 0; i < deviceInfoList.size(); i++) {
                deviceInfo = deviceInfoList.get(i);
                if (i == 0) {
                    //第一个为长机
                    if (droneMap.containsKey(deviceInfo.getDeviceId())) {
                        DroneControl droneControl = droneMap.get(deviceInfo.getDeviceId());
                        rt = droneControl.resumeMission(deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //集群回家返航
    public boolean goHomeCluster(MissionParam missionParam) {
        boolean rt = false;
        try {
            //给长机发送goHome指令，给僚机发送hold指令
            List<DeviceInfo> deviceInfoList = missionParam.getDevice();
            DeviceInfo deviceInfo = null;
            for (int i = 0; i < deviceInfoList.size(); i++) {
                deviceInfo = deviceInfoList.get(i);
                if (i == 0) {
                    //第一个为长机
                    if (droneMap.containsKey(deviceInfo.getDeviceId())) {
                        DroneControl droneControl = droneMap.get(deviceInfo.getDeviceId());
                        rt = droneControl.goHome(deviceInfo.getSysId(), MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //指点飞行
    public boolean gotoPosition(int deviceId, int targetSystemId, int targetComponentId, double lat, double lng, float alt) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.gotoPosition(targetSystemId, targetComponentId, lat, lng, alt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    @Override
    public void onDeviceStateChanged(int deviceId, int state) {
        try {
            synchronized (synObject) {
                //扫描监测集群swarmState表，判断集群任务是否结束，若结束，则删除集群配置文件
                boolean isDone = true;
                List<DeviceState> deviceStateList = null;
                DeviceState deviceState = null;
                //待清理掉的swamState的ID列表
                List<Integer> removeList = new ArrayList<>();
                removeList.clear();
                for (SwarmState swarmState : swarmStateMap.values()) {
                    isDone = true;
                    deviceStateList = swarmState.getDeviceStateList();
                    for (int i = 0; i < deviceStateList.size(); i++) {
                        deviceState = deviceStateList.get(i);
                        if (deviceState.getDeviceId() == deviceId) {
                            //state为1，代表任务结束，为0表示未结束
                            deviceState.setTaskDone(state == 1);
                            if (state == 0) {
                                //如果该设备状态为0，则代表未结束，直接返回，不必继续查找，已找到该设备
                                return;
                            }
                        }
                        if (deviceState.getTaskDone() == false) {
                            isDone = false;
                        }
                    }
                    if (isDone) {
                        //该集群对应的无人机都已经完成任务，表示该集群任务结束，删除集群配置文件
                        doDeleteSwarmFlight(swarmState.getSwarmId());
                        removeList.add(swarmState.getSwarmId());
                    }
                }
                //清理swamState列表
                for (Integer swamId : removeList) {
                    swarmStateMap.remove(swamId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
