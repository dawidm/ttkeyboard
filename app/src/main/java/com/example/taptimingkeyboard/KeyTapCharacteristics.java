package com.example.taptimingkeyboard;

public class KeyTapCharacteristics {

    private TTKeyboardButton ttKeyboardButton;
    private long holdTimeMillis;
    private float pressure;

    public KeyTapCharacteristics(TTKeyboardButton ttKeyboardButton, long holdTimeMillis, float pressure) {
        this.ttKeyboardButton = ttKeyboardButton;
        this.holdTimeMillis = holdTimeMillis;
        this.pressure=pressure;
    }
}
