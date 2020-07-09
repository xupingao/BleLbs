package com.ustc.location.location;

import java.io.Serializable;

public class UserPos implements Serializable {
    private String userid;
    private Position pos;

    public UserPos(String userid, Position pos) {
        this.userid = userid;
        this.pos = pos;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public Position getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return "id:"+ userid +"  ( "+ pos.getPosX() + "，" + pos.getPosY()+ ")";
    }
}
