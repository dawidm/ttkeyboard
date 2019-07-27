package com.example.taptimingkeyboard;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flight_time_characteristics")
public class FlightTimeCharacteristics {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private char charFrom;
    private char charTo;
    private double distanceMm;
    private long timeMillis;
    private String userId;
    private long sessionId;

    public FlightTimeCharacteristics(char charFrom, char charTo, double distanceMm, long timeMillis, String userId, long sessionId) {
        this.charFrom=charFrom;
        this.charTo=charTo;
        this.distanceMm = distanceMm;
        this.timeMillis = timeMillis;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public char getCharFrom() {
        return charFrom;
    }

    public char getCharTo() {
        return charTo;
    }

    public double getDistanceMm() {
        return distanceMm;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public String getUserId() {
        return userId;
    }

    public long getSessionId() {
        return sessionId;
    }
}
