package com.example.taptimingkeyboard;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public class TapTimingKeyboard {

    private View tapTimingKeyboardView;

    public TapTimingKeyboard(Context context) {
        this.tapTimingKeyboardView = TTKeyboardLayout.qwertyLayout().generateView(context, new TTKeyboardMotionEventListener() {
            @Override
            public void onMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {

            }
        });
    }

    public View getView() {
        return tapTimingKeyboardView;
    }

}
