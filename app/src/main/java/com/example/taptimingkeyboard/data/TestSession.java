package com.example.taptimingkeyboard.data;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "test_sessions")
public class TestSession {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long userId;
    private long timestampMs;
    @Nullable
    private long sessionEndTimestampMs;
    private String wordlistName;
    private int wordlistHashCode;
    @Nullable
    private int numErrors;
    @Nullable
    private Boolean isOrientationLandscape;
    @Nullable
    private String phoneInfo;

    public TestSession(long userId, long timestampMs, String wordlistName, int wordlistHashCode, @Nullable Boolean isOrientationLandscape, @Nullable String phoneInfo) {
        this.userId = userId;
        this.timestampMs = timestampMs;
        this.wordlistName = wordlistName;
        this.wordlistHashCode = wordlistHashCode;
        this.isOrientationLandscape = isOrientationLandscape;
        this.phoneInfo = phoneInfo;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
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

    public int getNumErrors() {
        return numErrors;
    }

    @Nullable
    public Boolean getOrientationLandscape() {
        return isOrientationLandscape;
    }

    @Nullable
    public String getPhoneInfo() {
        return phoneInfo;
    }

    public void setSessionEndTimestampMs(long sessionEndTimestampMs) {
        this.sessionEndTimestampMs = sessionEndTimestampMs;
    }

    public void setNumErrors(int numErrors) {
        this.numErrors = numErrors;
    }
}
