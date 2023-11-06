package com.hkxx.drone.db.entity;

import java.util.List;

public class CommandTaskContent {

    private List<UserSimpleInfo> users;

    public List<UserSimpleInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserSimpleInfo> users) {
        this.users = users;
    }
}
