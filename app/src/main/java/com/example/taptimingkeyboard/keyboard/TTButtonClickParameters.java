package com.example.taptimingkeyboard.keyboard;

public class TTButtonClickParameters {

    private final TTKeyboardButton ttKeyboardButton;
    private final long pressEventTimeMillis;
    private final float pressure;
    private final float x;
    private final float y;

    private boolean committed = false;
    private long clickId;
    private boolean released = false;
    private long releasedEventTime;
    private boolean flightTimeAborted=false;

    public TTButtonClickParameters(TTKeyboardButton ttKeyboardButton, long pressEventTimeMillis, float pressure, float x, float y) {
        this.ttKeyboardButton = ttKeyboardButton;
        this.pressEventTimeMillis = pressEventTimeMillis;
        this.pressure = pressure;
        this.x = x;
        this.y = y;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(long clickId) {
        committed=true;
        this.clickId=clickId;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(long releasedEventTime) {
        this.released = true;
        this.releasedEventTime=releasedEventTime;
    }

    public boolean isFlightTimeAborted() {
        return flightTimeAborted;
    }

    public void abortFlightTime() {
        flightTimeAborted=true;
    }

    public long getReleasedEventTime() {
        return releasedEventTime;
    }

    public long getClickId() {
        return clickId;
    }

    public TTKeyboardButton getTtKeyboardButton() {
        return ttKeyboardButton;
    }

    public long getPressEventTimeMillis() {
        return pressEventTimeMillis;
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
