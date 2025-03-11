#!/usr/bin/env bash

bash -c "export ROS_MASTER_URI=http://localhost:__MAVROS_PORT__ && export ROS_MASTER_URL=http://localhost:__MAVROS_PORT__ && source __ROS_SETUP__ && roslaunch mavros px4.launch tgt_system:=1 system_id:=3 fcu_url:="tcp://__LINKHUB_HOST__:__LINKHUB_PORT__" port:=__WEBSOCKET_PORT__"
