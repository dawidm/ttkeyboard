package com.example.taptimingkeyboard;

public class KeyDownParameters {

    private long timeMillis;
    private float pressure;
    private float x;
    private float y;

    public KeyDownParameters(long timeMillis, float pressure, float x, float y) {
        this.timeMillis = timeMillis;
        this.pressure = pressure;
        this.x = x;
        this.y = y;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public float getPressure() {
        return pressure;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
