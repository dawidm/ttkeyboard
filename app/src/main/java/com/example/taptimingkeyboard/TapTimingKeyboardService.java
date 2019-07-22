package com.example.taptimingkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;

public class TapTimingKeyboardService extends InputMethodService {


    @Override
    public View onCreateInputView() {
        KeyboardView keyboardView = new KeyboardView(this);
        return keyboardView;
    }
}
