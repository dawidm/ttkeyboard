package com.example.taptimingkeyboard.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.example.taptimingkeyboard.data.RemotePreferences;
import com.example.taptimingkeyboard.data.WordLists;

/**
 * Gets remote settings and word list from server.
 */
public class RemoteSettingsLoader {

    public static final String TAG = RemoteSettingsLoader.class.getName();

    /**
     * For notification about getting settings and word lists successfully and sending passing objects containing data.
     */
    public interface SuccessfulLoadListener {
        void onSettingsLoaded (RemotePreferences remotePreferences, WordLists wordLists);
    }

    /**
     * For notification about failure when getting data from server.
     */
    public interface FailureListener {
        void onFailure(Exception e);
    }

    public static final String WORDLIST_REMOTE_JSON_FILE = "ttwordlists.json";
    public static final String SETTINGS_REMOTE_JSON_FILE = "ttsettings.json";

    private Context applicationContext;

    private SuccessfulLoadListener successfulLoadListener;
    private FailureListener failureListener;


    /**
     * Instantiates a new Remote settings loader.
     *
     * @param applicationContext The application context.
     */
    public RemoteSettingsLoader(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Loads settings from remote url stored in application's SharedPreferences. Notifies listeners about the result.
     */
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

    /**
     * Sets the {@link SuccessfulLoadListener} to be notified about results.
     * @param successfulLoadListener The listener.
     * @return This RemotePreferences instance.
     */
    public RemoteSettingsLoader subscribeOnSuccessfulLoad(SuccessfulLoadListener successfulLoadListener) {
        this.successfulLoadListener = successfulLoadListener;
        return this;
    }

    /**
     * Sets {@link FailureListener} to be notified about results.
     * @param failureListener The listener.
     * @return This RemotePreferences instance.
     */
    public RemoteSettingsLoader subscribeOnFailure(FailureListener failureListener) {
        this.failureListener=failureListener;
        return this;
    }

}
