package com.ustc.location.location;

import java.io.Serializable;

public class Anchor implements Serializable {
    private String mac;
    private Position pos;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public Position getPos() {
        return pos;
    }

    public Anchor(String mac, Position pos) {
        this.pos = pos;
        this.mac = mac;
    }
    @Override
    public String toString() {
        return "max:"+mac+"  ( "+ pos.getPosX() + "，" + pos.getPosY()+ ")";
    }
}
