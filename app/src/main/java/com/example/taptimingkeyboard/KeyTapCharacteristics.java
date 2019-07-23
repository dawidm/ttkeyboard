package com.example.taptimingkeyboard;

public class KeyTapCharacteristics {

    private TTKeyboardButton ttKeyboardButton;
    private long holdTimeNanos;
    private float pressure;

    public KeyTapCharacteristics(TTKeyboardButton ttKeyboardButton, long holdTimeNanos, float pressure) {
        this.ttKeyboardButton = ttKeyboardButton;
        this.holdTimeNanos=holdTimeNanos;
        this.pressure=pressure;
    }
}
