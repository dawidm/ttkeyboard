package com.example.taptimingkeyboard;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TapTimingKeyboardService extends InputMethodService {

    public static final String TAG = TapTimingKeyboardService.class.getName();

    private TapTimingKeyboard tapTimingKeyboard;

    @Override
    public void onCreate() {
        super.onCreate();
        tapTimingKeyboard = new TapTimingKeyboard(this, new TTKeyboardMotionEventListener() {
            @Override
            public void onMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {
                handleMotionEvent(ttButton,motionEvent);
            }
        });
    }

    @Override
    public View onCreateInputView() {
        return tapTimingKeyboard.getView();
    }

    private void handleMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {

    }
}
