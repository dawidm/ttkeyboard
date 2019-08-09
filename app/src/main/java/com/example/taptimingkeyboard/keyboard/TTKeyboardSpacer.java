package com.example.taptimingkeyboard.keyboard;

public class TTKeyboardSpacer extends TTKeyboardElement {

    //as a proportion of standard button size
    private float size;

    public TTKeyboardSpacer(float size) {
        this.size = size;
    }

    public float getSize() {
        return size;
    }
}
