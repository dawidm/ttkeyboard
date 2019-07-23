package com.example.taptimingkeyboard;

public class TTKeyboardButton extends TTKeyboardElement {

    private String label;
    private int code;
    //proportion of standard button size
    private float size=1.0f;

    public TTKeyboardButton(String label, int code) {
        this.label = label;
        this.code = code;
    }

    public TTKeyboardButton(String label, int code, float size) {
        this.label = label;
        this.code = code;
        this.size = size;
    }

    public TTKeyboardButton(char label) {
        this.label = ""+label;
        this.code = label;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public float getSize() {
        return size;
    }
}
