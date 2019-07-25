package com.example.taptimingkeyboard;

public class FlightTimeCharacteristics {
    private TTKeyboardButton buttonFrom;
    private TTKeyboardButton buttonTo;
    private double distanceMm;
    private long timeMillis;

    public FlightTimeCharacteristics(TTKeyboardButton buttonFrom, TTKeyboardButton buttonTo, double distanceMm, long timeMillis) {
        this.buttonFrom = buttonFrom;
        this.buttonTo = buttonTo;
        this.distanceMm = distanceMm;
        this.timeMillis = timeMillis;
    }
}
