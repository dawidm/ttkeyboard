package com.example.taptimingkeyboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.example.taptimingkeyboard.R;

public class PreferencesActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    public static class TapTimingKeyboardPreferenceFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_screen,rootKey);
            ((EditTextPreference)findPreference("user_id")).setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
            });
            ((EditTextPreference)findPreference("remote_url")).setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
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
        replacePreferencesFragment();
        preferenceChangeListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if(s.equals("remote_url")) {
                    String newUrl = sharedPreferences.getString(s,"");
                    if(newUrl.charAt(newUrl.length()-1)!='/')
                        newUrl=newUrl+'/';
                    sharedPreferences.edit().putString("remote_url",newUrl).commit();
                    replacePreferencesFragment();
                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void replacePreferencesFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new TapTimingKeyboardPreferenceFragment())
                .commit();
    }
}
