package com.example.taptimingkeyboard.data;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "test_sessions")
public class TestSession {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String userId;
    private long timestampMs;
    @Nullable
    private long sessionEndTimestampMs;
    private String wordlistName;
    private int wordlistHashCode;
    @Nullable
    private int sessionFailedTries;

    public TestSession(String userId, long timestampMs, String wordlistName, int wordlistHashCode) {
        this.userId = userId;
        this.timestampMs = timestampMs;
        this.wordlistName = wordlistName;
        this.wordlistHashCode = wordlistHashCode;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public long getSessionEndTimestampMs() {
        return sessionEndTimestampMs;
    }

    public String getWordlistName() {
        return wordlistName;
    }

    public int getWordlistHashCode() {
        return wordlistHashCode;
    }

    public int getSessionFailedTries() {
        return sessionFailedTries;
    }

    public void setSessionEndTimestampMs(long sessionEndTimestampMs) {
        this.sessionEndTimestampMs = sessionEndTimestampMs;
    }

    public void setSessionFailedTries(int sessionFailedTries) {
        this.sessionFailedTries = sessionFailedTries;
    }
}
