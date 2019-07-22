package com.example.taptimingkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;

public class TapTimingKeyboardService extends InputMethodService {

    private TapTimingKeyboard tapTimingKeyboard;

    @Override
    public void onCreate() {
        super.onCreate();
        tapTimingKeyboard = new TapTimingKeyboard(this);
    }

    @Override
    public View onCreateInputView() {
        return tapTimingKeyboard.getView();
    }
}
