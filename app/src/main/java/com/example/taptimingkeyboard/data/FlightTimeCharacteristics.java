package com.example.taptimingkeyboard.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flight_time_characteristics")
public class FlightTimeCharacteristics {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long timestamp;
    private char charFrom;
    private char charTo;
    private double distanceMm;
    private long timeMillis;
    private String userId;
    private long sessionId;

    public FlightTimeCharacteristics(long timestamp, char charFrom, char charTo, double distanceMm, long timeMillis, String userId, long sessionId) {
        this.timestamp = timestamp;
        this.charFrom = charFrom;
        this.charTo = charTo;
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

    public long getTimestamp() {
        return timestamp;
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