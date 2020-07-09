package com.ustc.location.location;

import java.io.Serializable;

public class Position implements Serializable {

    private double posX, posY;

    public Position(double x, double y) {
        posX = x; posY = y;
    }
    void setPosX(double x) {
        posX = x;
    }
    void setPosY(double y) {
        posY = y;
    }
    public double getPosX() {
        return posX;
    }
    public double getPosY() {
        return posY;
    }

    @Override
    public String toString() {
        return "用户当前位置为：(" + posX + "，" + posY + ")";
    }
}
