package com.example.taptimingkeyboard;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "key_tap_characteristics")
public class KeyTapCharacteristics {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private char keyCharacter;
    private long holdTimeMillis;
    private float pressure;
    private String userId;
    private long sessionId;

    public KeyTapCharacteristics(char keyCharacter, long holdTimeMillis, float pressure, String userId, long sessionId) {
        this.keyCharacter=keyCharacter;
        this.holdTimeMillis = holdTimeMillis;
        this.pressure=pressure;
        this.userId=userId;
        this.sessionId=sessionId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public char getKeyCharacter() {
        return keyCharacter;
    }

    public long getHoldTimeMillis() {
        return holdTimeMillis;
    }

    public float getPressure() {
        return pressure;
    }

    public String getUserId() {
        return userId;
    }

    public long getSessionId() {
        return sessionId;
    }
}
