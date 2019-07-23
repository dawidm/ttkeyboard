package com.example.taptimingkeyboard;

import android.content.Context;
import android.view.View;

public class TapTimingKeyboard {

    private View tapTimingKeyboardView;

    public TapTimingKeyboard(Context context, TTKeyboardMotionEventListener motionEventListener) {
        this.tapTimingKeyboardView = TTKeyboardLayout.qwertyLayout().generateView(context,motionEventListener);
    }

    public View getView() {
        return tapTimingKeyboardView;
    }

}
