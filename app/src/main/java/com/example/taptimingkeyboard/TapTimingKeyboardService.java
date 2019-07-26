package com.example.taptimingkeyboard;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;

public class TapTimingKeyboardService extends InputMethodService {

    public static final String TAG = TapTimingKeyboardService.class.getName();

    private TapTimingKeyboard tapTimingKeyboard;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public View onCreateInputView() {
        Log.d(TAG,"onCreateInputView");
        tapTimingKeyboard = new TapTimingKeyboard(this, TTKeyboardLayout.Layout.SIMPLEST_QWERTY_SYMMETRIC, new TTKeyboardClickListener() {
            @Override
            public void onKeyboardClick(TTKeyboardButton ttButton) {
                handleKeyboardClick(ttButton);
            }
        });
        return tapTimingKeyboard.getView();
    }

    private void handleKeyboardClick(TTKeyboardButton ttButton) {
        getCurrentInputConnection().commitText(""+(char)ttButton.getCode(),1);
    };

}
