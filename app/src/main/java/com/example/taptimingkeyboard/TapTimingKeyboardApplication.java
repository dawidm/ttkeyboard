package com.example.taptimingkeyboard;

import android.app.Application;

import androidx.preference.PreferenceManager;

public class TapTimingKeyboardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.preference_screen, false);
    }
}
