package com.example.taptimingkeyboard;

import android.view.MotionEvent;

public interface TTKeyboardMotionEventListener {
    public void onMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent);
}
