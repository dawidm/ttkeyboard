package com.example.taptimingkeyboard;

import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

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
        tapTimingKeyboard = new TapTimingKeyboard(this, TTKeyboardLayout.Layout.SIMPLEST_QWERTY_SYMMETRIC, new TTKeyboardClickListener() {
            @Override
            public void onKeyboardClick(TTKeyboardButton ttButton, long clickId) {
                handleKeyboardClick(ttButton);
            }
        });
        return tapTimingKeyboard.getView();
    }

    private void handleKeyboardClick(TTKeyboardButton ttButton) {
        getCurrentInputConnection().commitText(""+(char)ttButton.getCode(),1);
    };

}
