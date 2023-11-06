package com.hkxx.drone.joystick;

public class ButtonMap {
    private int btNum = 0; //按钮编号
    private String act = ""; //按钮动作值

    public ButtonMap(int btNum, String act) {
        this.btNum = btNum;
        this.act = act;
    }

    public int getBtNum() {
        return btNum;
    }

    public void setBtNum(int btNum) {
        this.btNum = btNum;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }
}
