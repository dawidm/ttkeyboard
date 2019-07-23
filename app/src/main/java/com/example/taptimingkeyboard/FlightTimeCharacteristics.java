package com.example.taptimingkeyboard;

public class FlightTimeCharacteristics {
    private TTKeyboardButton buttonFrom;
    private TTKeyboardButton buttonTo;
    private double distancePx;
    private long timeMillis;

    public FlightTimeCharacteristics(TTKeyboardButton buttonFrom, TTKeyboardButton buttonTo, double distancePx, long timeMillis) {
        this.buttonFrom = buttonFrom;
        this.buttonTo = buttonTo;
        this.distancePx = distancePx;
        this.timeMillis = timeMillis;
    }
}
