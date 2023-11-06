package com.hkxx.drone;

import com.MAVLink.enums.MAV_COMPONENT;
import com.alibaba.fastjson.JSON;
import com.hkxx.drone.db.MybatisUtil;
import com.hkxx.drone.db.dao.*;
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
    // Ports value - start from
    int from = 58000;
    // Ports value - to
    int to = 58100;

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
            initPorts(from, to);
            tCheckDroneState = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("DroneService is started.");

                    boolean isExist = false;
                    //待删除的控制对象id列表
                    List<Integer> removeList = new ArrayList<>();
                    //当前最新的控制对象id列表，droneMap中不在该列表中的对象，需要清理掉
                    List<Integer> currentList = new ArrayList<>();
                    //清空所有的linkhub配置文件
                    try {
                        LinkhubProfileManager.removeAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int[] controlTypes = {ControlType.CONTROL_CLUSTER, ControlType.CONTROL_DIRECT};
                    while (!isStop) {
                        SqlSession sqlSession = null;
                        currentList.clear();
                        try {
                            sqlSession = MybatisUtil.getSqlSession();
                            //扫描无人机表，添加控制对象
                            DroneDao droneDao = sqlSession.getMapper(DroneDao.class);
                            List<DroneEntity> droneList = droneDao.selectByControlTypes(controlTypes);
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
                                if (drone.getControlType() == ControlType.CONTROL_CLUSTER) {
                                    //若是集群控制，则检测该无人机是否分配linkhub端口，若没有，则分配端口
                                    droneControl.setLinkhubPort(genLinkhubPort(drone.getDeviceId()));
                                }
                                droneControl.setDeviceId(drone.getDeviceId()); //保存设备ID
                                droneControl.setDeviceStateChangedListener(DroneService.this);
                                droneControl.setDeviceType(DeviceType.DRONE);
                                droneControl.setControlType(drone.getControlType());
                                currentList.add(drone.getDeviceId()); //加入到当前控制对象id列表
                                if (droneMap.containsKey(drone.getDeviceId())) {
                                    //检测无人机的配置是否有变化，若有变化则更新linkhub配置文件，通知linkhub更新服务
                                    DroneControl controlInMap = droneMap.get(drone.getDeviceId());
                                    if (!controlInMap.isEqual(droneControl)) {
                                        //更新linkhub配置文件，先删除配置文件，若控制类型为集群控制，则生成新的配置文件
                                        doDeleteDeviceProfile(drone.getDeviceId());
                                        if (drone.getControlType() == ControlType.CONTROL_CLUSTER) {
                                            //若控制类型为集群控制，则生成新的配置文件
                                            updateLinkhubProfile(drone.getDeviceId(), drone.getName(), droneControl.getDevIP(), droneControl.getDevPort(), droneControl.getLinkhubPort(), DeviceType.DRONE);
                                        }
                                        droneMap.put(drone.getDeviceId(), droneControl);
                                        droneControl.stopConnection();//停止连接
                                        droneControl.startConnection();//开启连接
                                    } else if (controlInMap.getControlType() == ControlType.CONTROL_CLUSTER) {
                                        //若控制类型为集群控制，检测linkhub配置文件是否存在，若不存在，则创建，防止配置文件丢失
                                        if (!LinkhubProfileManager.isProfileExist(Worker.makeDroneProfileName(controlInMap.getDeviceId()))) {
                                            //若控制类型为集群控制，则生成新的配置文件
                                            updateLinkhubProfile(drone.getDeviceId(), drone.getName(), droneControl.getDevIP(), droneControl.getDevPort(), droneControl.getLinkhubPort(), DeviceType.DRONE);
                                        }
                                    }
                                } else {
                                    //若不存在，则添加无人机控制对象至droneMap，建立连接
                                    if (drone.getControlType() == ControlType.CONTROL_CLUSTER) {
                                        //更新linkhub配置文件
                                        updateLinkhubProfile(drone.getDeviceId(), drone.getName(), droneControl.getDevIP(), droneControl.getDevPort(), droneControl.getLinkhubPort(), DeviceType.DRONE);
                                    }
                                    droneMap.put(drone.getDeviceId(), droneControl);
                                    droneControl.startConnection();//开启连接
                                }
                            }
                            //扫描无人艇列表，添加无人艇控制对象
                            sqlSession = MybatisUtil.getSqlSession();
                            BoatDao boatDao = sqlSession.getMapper(BoatDao.class);
                            List<BoatEntity> boatList = boatDao.selectByControlTypes(controlTypes);
                            BoatEntity boat = null;
                            DroneControl boatControl = null;
                            //加载新的无人艇连接对象
                            for (int i = 0; i < boatList.size(); i++) {
                                boat = boatList.get(i);
                                boatControl = new DroneControl();
                                DeviceLinkAddress linkAddress = DeviceLinkAddress.parse(boat.getLinkaddress());
                                if (linkAddress.host.equals(DeviceLinkAddress.HOST_LOCALHOST_IP) || linkAddress.port == DeviceLinkAddress.PORT_TOGEN) {
                                    log.info("Device is using default localhost and port,please check if it is right.Drone's deviceId:" + boat.getDeviceId() + " Name:" + boat.getName());
                                }
                                boatControl.setDevIP(linkAddress.host);
                                boatControl.setDevPort(linkAddress.port);
                                boatControl.setLinkhubIP(Config.linkhubIP);
                                boatControl.setSessionTimeout(Config.sessionTimeout);
                                if (boat.getControlType() == ControlType.CONTROL_CLUSTER) {
                                    //若是集群控制，则检测该无人机是否分配linkhub端口，若没有，则分配端口
                                    boatControl.setLinkhubPort(genLinkhubPort(boat.getDeviceId()));
                                }
                                boatControl.setDeviceId(boat.getDeviceId()); //保存设备ID
                                boatControl.setDeviceStateChangedListener(DroneService.this);
                                boatControl.setDeviceType(DeviceType.BOAT);
                                boatControl.setControlType(boat.getControlType());
                                currentList.add(boat.getDeviceId()); //加入到当前控制对象id列表
                                if (droneMap.containsKey(boat.getDeviceId())) {
                                    //检测无人机的配置是否有变化，若有变化则更新linkhub配置文件，通知linkhub更新服务
                                    DroneControl controlInMap = droneMap.get(boat.getDeviceId());
                                    if (!controlInMap.isEqual(boatControl)) {
                                        //更新linkhub配置文件，先删除配置文件，若控制类型为集群控制，则生成新的配置文件
                                        doDeleteDeviceProfile(boat.getDeviceId());
                                        if (boat.getControlType() == ControlType.CONTROL_CLUSTER) {
                                            //若控制类型为集群控制，则生成新的配置文件
                                            updateLinkhubProfile(boat.getDeviceId(), boat.getName(), boatControl.getDevIP(), boatControl.getDevPort(), boatControl.getLinkhubPort(), DeviceType.BOAT);
                                        }
                                        droneMap.put(boat.getDeviceId(), boatControl);
                                        boatControl.stopConnection();//停止连接
                                        boatControl.startConnection();//开启连接
                                    } else if (controlInMap.getControlType() == ControlType.CONTROL_CLUSTER) {
                                        //若控制类型为集群控制，检测linkhub配置文件是否存在，若不存在，则创建，防止配置文件丢失
                                        if (!LinkhubProfileManager.isProfileExist(Worker.makeDroneProfileName(controlInMap.getDeviceId()))) {
                                            //若控制类型为集群控制，则生成新的配置文件
                                            updateLinkhubProfile(boat.getDeviceId(), boat.getName(), boatControl.getDevIP(), boatControl.getDevPort(), boatControl.getLinkhubPort(), DeviceType.BOAT);
                                        }
                                    }
                                } else {
                                    //若不存在，则添加无人机控制对象至droneMap，建立连接
                                    if (boat.getControlType() == ControlType.CONTROL_CLUSTER) {
                                        //更新linkhub配置文件
                                        updateLinkhubProfile(boat.getDeviceId(), boat.getName(), boatControl.getDevIP(), boatControl.getDevPort(), boatControl.getLinkhubPort(), DeviceType.BOAT);
                                    }
                                    droneMap.put(boat.getDeviceId(), boatControl);
                                    boatControl.startConnection();//开启连接
                                }
                            }
                            //比对当前无人机控制列表，清理已经删掉的无人机连接对象
                            removeList.clear();
                            for (Integer deviceId : droneMap.keySet()) {
                                isExist = false;
                                for (int i = 0; i < currentList.size(); i++) {
                                    if (deviceId == currentList.get(i)) {
                                        isExist = true;
                                        break;
                                    }
                                }
                                if (!isExist) {
                                    //需要清理掉当前的无人机连接对象
                                    removeList.add(deviceId);
                                }
                            }
                            //清理无人机连接对象
                            for (Integer deviceId : removeList) {
                                DroneControl clearDroneControl = droneMap.get(deviceId);
                                clearDroneControl.stopConnection();
                                if (clearDroneControl.getControlType() == ControlType.CONTROL_CLUSTER) {
                                    //删除linkhub配置文件
                                    doDeleteDeviceProfile(clearDroneControl.getDeviceId());
                                }
                                clearDroneControl = null;
                                droneMap.remove(deviceId);
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

    /*
     * Insert available Ports into DB. This is important for a new deployment.
     */
    public static void initPorts(int from, int to) {
        //检测port表是否为空，若为空则初始化port表，插入一系列可用端口；若不为空，则不处理
        try {
            SqlSession sqlSession = MybatisUtil.getSqlSession();
            PortDao portDao = sqlSession.getMapper(PortDao.class);
            List<PortEntity> portEntities = portDao.selectList(null);
            if (portEntities.size() == 0) {
                if (from <= 0 || from >= 65000 || to <= 0 || to >= 65000 || from > to) {
                    return;
                }
                int cntSucc = 0;
                for (int port = from; port < to; port++) {
                    PortEntity p = new PortEntity();
                    p.setAvailable(1);
                    p.setCategory(Constants.CATEGORY_NONE);
                    p.setValue(port);
                    p.setReplayId(null);
                    p.setDeviceId(null);
                    portDao.insert(p);
                    sqlSession.commit();
                    if (p.getId() != null) {
                        cntSucc++;
                    }
                }
                System.out.println("Inserted ports:" + cntSucc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MybatisUtil.close();
        }

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

    private static boolean doDeleteDeviceProfile(int deviceId) {
        try {
            String name = Worker.makeDroneProfileName(deviceId);
            LinkhubProfileManager.deleteProfile(name);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void updateLinkhubProfile(int deviceId, String deviceName, String deviceHost, int devicePort, int linkhubPort, int deviceType) {
        try {
            String droneProfileName = "";
            switch (deviceType) {
                case DeviceType.BOAT:
                    droneProfileName = Worker.makeBoatProfileName(deviceId);
                    break;
                case DeviceType.DRONE:
                default:
                    droneProfileName = Worker.makeDroneProfileName(deviceId);
                    break;
            }


            LinkhubProfile profile = null;

            if (Config.deviceUseRTCP) {
                profile = new DroneOutIn2Profile(droneProfileName,
                        deviceName, Calendar.getInstance().getTime()
                        .toGMTString(), "0", "device", deviceHost,
                        String.valueOf(devicePort), "1", "client", String.valueOf(linkhubPort),
                        Config.mavlogWorkspace,
                        Worker.makeDroneMavlogPrefix(deviceId));
            } else {
                profile = new DroneOutIn1Profile(droneProfileName,
                        deviceName, Calendar.getInstance().getTime()
                        .toGMTString(), "0", "device", deviceHost,
                        String.valueOf(devicePort), "1", "client", String.valueOf(linkhubPort),
                        Config.mavlogWorkspace,
                        Worker.makeDroneMavlogPrefix(deviceId));
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

    //拍照
    public boolean shootPhoto(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.shootPhoto(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //回中
    public boolean cameraCenter(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.cameraCenter(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //切换相机模式
    public boolean changeCameraMode(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.changeCameraMode(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //开始或停止录像
    public boolean triggerVideo(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.triggerVideo(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //夜视模式
    public boolean ircNight(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.ircNight(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //白天模式
    public boolean ircDay(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.ircDay(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //自动切换黑夜模式
    public boolean ircAuto(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.ircAuto(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //放大
    public boolean zoomPlus(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.zoomPlus(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //缩小
    public boolean zoomMinus(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.zoomMinus(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //增加焦距
    public boolean focusPlus(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.focusPlus(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //减小焦距
    public boolean focusMinus(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.focusMinus(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //自动对焦
    public boolean focusAuto(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.focusAuto(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //朝向模式锁头
    public boolean cameraLockup(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.cameraLockUp(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //朝向模式跟随
    public boolean cameraFollow(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.cameraFollow(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //跟踪
    public boolean cameraTrack(int deviceId, int targetSystemId, int targetComponentId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.cameraTrack(targetSystemId, targetComponentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //转动云台
    public boolean moveGimbal(int deviceId, int targetSystemId, int targetComponentId, float pitch, float yaw) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.moveGimbal(targetSystemId, targetComponentId, pitch, yaw);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //打开抛投
    public boolean openThrower(int deviceId, int targetSystemId, int targetComponentId, float channelId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.setChannelState(targetSystemId, targetComponentId, 1, channelId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //关闭抛投
    public boolean closeThrower(int deviceId, int targetSystemId, int targetComponentId, float channelId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.setChannelState(targetSystemId, targetComponentId, 0, channelId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    //设置通道状态
    public boolean setChannelState(int deviceId, int targetSystemId, int targetComponentId, float state, float channelId) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.setChannelState(targetSystemId, targetComponentId, state, channelId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    public boolean manualControl(int deviceId, int deviceType, int targetSystemId, int targetComponentId, int x, int y, int z, int r) {
        boolean rt = false;
        try {
            if (droneMap.containsKey(deviceId)) {
                DroneControl droneControl = droneMap.get(deviceId);
                rt = droneControl.manualControl(deviceType, targetSystemId, targetComponentId, x, y, z, r);
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
