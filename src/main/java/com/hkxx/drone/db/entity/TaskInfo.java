package com.hkxx.drone.db.entity;

public class TaskInfo {
    private String taskType = "";
    private Object content;

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
