package com.example.taptimingkeyboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class TapTimingKeyboard {

    private View tapTimingKeyboardView;

    public TapTimingKeyboard(Context context) {
        this.tapTimingKeyboardView = LayoutInflater.from(context).inflate(R.layout.tt_keyboard_layout,null);
    }

    public View getView() {
        return tapTimingKeyboardView;
    }
}
