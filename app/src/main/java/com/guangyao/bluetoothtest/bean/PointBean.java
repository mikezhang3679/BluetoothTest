package com.guangyao.bluetoothtest.bean;

/**
 * Created by liuqiong on 2017/5/16.
 */

public class PointBean {
    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "PointBean{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
