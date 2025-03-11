#!/usr/bin/env bash

source __ROS_SETUP__
export ROS_PACKAGE_PATH=$ROS_PACKAGE_PATH:__ROS_PACKAGE__
roslaunch ramy_dronecloud multi_uav_mavros_sitl_template.launch ID:=__SYS_ID__ fcu_url:=tcp://__LINKHUB_HOST__:__LINKHUB_PORT__ group_ns:=__GROUP_NS__

