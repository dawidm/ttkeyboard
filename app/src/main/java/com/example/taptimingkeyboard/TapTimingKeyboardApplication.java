package com.example.taptimingkeyboard;

import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.example.taptimingkeyboard.data.TapTimingDatabase;

public class TapTimingKeyboardApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.preference_screen, false);
        TapTimingDatabase.instance(getApplicationContext()).query("select 1",null); //init database
    }
}
