package com.example.taptimingkeyboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.example.taptimingkeyboard.R;

public class PreferencesActivity extends AppCompatActivity {

    public static class TapTimingKeyboardPreferenceFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_screen,rootKey);
            ((EditTextPreference)findPreference("longer_screen_dimension")).setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
            });
            ((EditTextPreference)findPreference("shorter_screen_dimension")).setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
            });
            ((SeekBarPreference)findPreference("height_landscape")).setShowSeekBarValue(true);
            ((SeekBarPreference)findPreference("height_portrait")).setShowSeekBarValue(true);
            ((SeekBarPreference)findPreference("click_volume")).setShowSeekBarValue(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new TapTimingKeyboardPreferenceFragment())
                .commit();
    }
}
