package com.example.taptimingkeyboard;

import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.example.taptimingkeyboard.keyboard.TTKeyboardButton;
import com.example.taptimingkeyboard.keyboard.TTKeyboardClickListener;
import com.example.taptimingkeyboard.keyboard.TTKeyboardLayout;
import com.example.taptimingkeyboard.keyboard.TapTimingKeyboard;

public class TapTimingKeyboardService extends InputMethodService {

    public static final String TAG = TapTimingKeyboardService.class.getName();

    private TapTimingKeyboard tapTimingKeyboard;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                Log.d(TAG,"onSharedPreferenceChanged");
                setInputView(onCreateInputView());
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public View onCreateInputView() {
        Log.d(TAG,"onCreateInputView");
        tapTimingKeyboard = new TapTimingKeyboard(this, TTKeyboardLayout.Layout.SIMPLE_QWERTY_SYMMETRIC, new TTKeyboardClickListener() {
            @Override
            public void onKeyboardClick(TTKeyboardButton ttButton, long clickId) {
                handleKeyboardClick(ttButton);
            }
        });
        return tapTimingKeyboard.getView();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        Log.d(TAG,"onStartInputView");
        super.onStartInputView(info, restarting);
        tapTimingKeyboard.abortCurrentFlightTime();
    }

    private void handleKeyboardClick(TTKeyboardButton ttButton) {
        switch (ttButton.getCode()) {
            case 8:
                getCurrentInputConnection().deleteSurroundingText(1, 0);
                break;
            case 13:
                getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));
                break;
            default:
                getCurrentInputConnection().commitText("" + (char) ttButton.getCode(), 1);
        }
    };

}
