package com.example.taptimingkeyboard.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.example.taptimingkeyboard.data.RemotePreferences;
import com.example.taptimingkeyboard.data.WordLists;

public class RemoteSettingsLoader {

    public static final String TAG = RemoteSettingsLoader.class.getName();

    public interface SuccessfulLoadListener {
        void onSettingsLoaded (RemotePreferences remotePreferences, WordLists wordLists);
    }

    public interface FailureListener {
        void onFailure(Exception e);
    }

    public static final String WORDLIST_REMOTE_JSON_FILE = "wordlists.json";
    public static final String SETTINGS_REMOTE_JSON_FILE = "ttsettings.json";

    private Context applicationContext;

    private SuccessfulLoadListener successfulLoadListener;
    private FailureListener failureListener;

    public RemoteSettingsLoader(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void loadAsync() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(applicationContext);
                    String serverUrl = sharedPreferences.getString("remote_url","");
                    String wordlistsUrl=serverUrl+WORDLIST_REMOTE_JSON_FILE;
                    Log.i(TAG,"getting wordlists from" + wordlistsUrl);
                    WordLists wordLists= WordLists.fromUrl(wordlistsUrl);
                    Log.i(TAG,"updated wordlists from" + wordlistsUrl);
                    String settingsUrl = serverUrl+SETTINGS_REMOTE_JSON_FILE;
                    Log.i(TAG,"getting remote settings from" + settingsUrl);
                    RemotePreferences remotePreferences=RemotePreferences.fromUrl(settingsUrl);
                    Log.i(TAG,"updated remote settings from" + settingsUrl);
                    if(successfulLoadListener !=null)
                        successfulLoadListener.onSettingsLoaded(remotePreferences,wordLists);
                } catch (Exception e) {
                    Log.w(TAG,"error updating remote data",e);
                    if(failureListener!=null)
                        failureListener.onFailure(e);
                }
            }
        });
    }

    public RemoteSettingsLoader subscribeOnSuccessfulLoad(SuccessfulLoadListener successfulLoadListener) {
        this.successfulLoadListener = successfulLoadListener;
        return this;
    }

    public RemoteSettingsLoader subscribeOnFailure(FailureListener failureListener) {
        this.failureListener=failureListener;
        return this;
    }

}
