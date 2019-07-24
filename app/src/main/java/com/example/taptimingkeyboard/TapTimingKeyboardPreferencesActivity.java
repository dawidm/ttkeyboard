package com.example.taptimingkeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.InputType;

public class TapTimingKeyboardPreferencesActivity extends PreferenceActivity {

    public static class TapTimingKeyboardPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_screen);
            ((EditTextPreference)findPreference("longer_screen_dimension")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            ((EditTextPreference)findPreference("shorter_screen_dimension")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_timing_keyboard_preferences);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new TapTimingKeyboardPreferenceFragment()).commit();
    }
}
