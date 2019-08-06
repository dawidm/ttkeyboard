package com.example.taptimingkeyboard;

public class TTButtonClick {

    private TTKeyboardButton ttButton;
    private long clickId;
    private long clickTimestampMillis;

    public TTButtonClick(TTKeyboardButton ttButton, long clickId, long clickTimestampMillis) {
        this.ttButton = ttButton;
        this.clickId = clickId;
        this.clickTimestampMillis = clickTimestampMillis;
    }

    public TTKeyboardButton getTtButton() {
        return ttButton;
    }

    public long getClickId() {
        return clickId;
    }

    public long getClickTimestampMillis() {
        return clickTimestampMillis;
    }
}
