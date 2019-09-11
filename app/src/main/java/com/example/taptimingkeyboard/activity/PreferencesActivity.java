package com.example.taptimingkeyboard.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.data.TapTimingDatabase;
import com.example.taptimingkeyboard.data.UserInfo;

/**
 * An activity for changing application's preferences.
 */
public class PreferencesActivity extends AppCompatActivity {

    public static final int CODE_USER_ID=1;
    public static final int CODE_RESULT_USER_ID=1;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    public static class TapTimingKeyboardPreferenceFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode==CODE_USER_ID && resultCode==CODE_RESULT_USER_ID) {
                long userId=data.getLongExtra("user_id",0);
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong("user_id",userId).commit();
                ((PreferencesActivity)getActivity()).replacePreferencesFragment();
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_screen,rootKey);
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final UserInfo userInfo=TapTimingDatabase.instance(getContext().getApplicationContext()).userInfoDao().getById(sharedPreferences.getLong("user_id",1));
                    if(userInfo!=null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findPreference("user_id").setSummary(userInfo.toString());
                            }
                        });
                }
            });
            findPreference("user_id").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(),UserInfoActivity.class);
                    intent.putExtra("started_from_preferences",true);
                    startActivityForResult(intent,CODE_USER_ID);
                    return false;
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
            ((SeekBarPreference)findPreference("vibration_duration")).setShowSeekBarValue(true);
            findPreference("unsent_results").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(),TestSessionsSyncActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
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
