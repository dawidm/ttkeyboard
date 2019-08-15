package com.example.taptimingkeyboard.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.data.RemotePreferences;
import com.example.taptimingkeyboard.keyboard.TTKeyboardButton;
import com.example.taptimingkeyboard.keyboard.TTKeyboardClickListener;
import com.example.taptimingkeyboard.keyboard.TTKeyboardLayout;
import com.example.taptimingkeyboard.keyboard.TapTimingKeyboard;
import com.example.taptimingkeyboard.data.WordLists;
import com.example.taptimingkeyboard.data.TapTimingDatabase;
import com.example.taptimingkeyboard.data.TestSession;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TestSessionActivity extends AppCompatActivity {

    public static final String TAG = TestSessionActivity.class.getName();

    public static final int TEST_WORD_BLINK_TIME_MILLIS = 1000;
    public static final String WORDLIST_REMOTE_JSON_FILE = "wordlists.json";
    public static final String SETTINGS_REMOTE_JSON_FILE = "ttsettings.json";

    private TapTimingKeyboard tapTimingKeyboard;
    private AudioManager audioManager;

    private WordLists wordLists;
    private RemotePreferences remotePreferences;
    private long sessionId;
    private boolean sessionActive=false;
    private ArrayList<Long> clicksIds = new ArrayList<>();
    private String[] words;
    private int wordsIterator;
    private char[] currentWord;
    private int charsIterator;
    private char currentChar = 0;

    private boolean sounds=false;
    private float soundsVol=0.0f;

    private TextView testWordTextView;
    private TextView sessionInfoTextView;
    private Button sessionStartButton;
    private LinearLayout buttonsContainer;
    private Spinner listsSpinner;
    private LinearLayout listLinearLayout;
    private LinearLayout contentContainer;
    private ConstraintLayout keyboardContainer;
    private ConstraintLayout getDataContainer;
    private TextView getDataTextView;
    private Button retryButton;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture testWordColorFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_session);
        loadPreferences();
        testWordTextView = findViewById(R.id.test_word_textview);
        testWordTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                endSession(true);
                return true;
            }
        });
        sessionInfoTextView = findViewById(R.id.session_info_textview);
        sessionStartButton = findViewById(R.id.start_button);
        sessionStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButtonClick(view);
            }
        });
        buttonsContainer=findViewById(R.id.buttons_container);
        listsSpinner=findViewById(R.id.lists_spinner);
        ArrayList<String> emptySpinnerArray = new ArrayList<>(1);
        emptySpinnerArray.add(getResources().getString(R.string.wordlist_spinner_empty));
        listsSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,emptySpinnerArray));
        listLinearLayout=findViewById(R.id.lists_linear_layout);
        contentContainer=findViewById(R.id.content_container);
        keyboardContainer=findViewById(R.id.keyboard_container);
        getDataContainer=findViewById(R.id.get_data_container);
        getDataTextView=findViewById(R.id.get_data_text_view);
        retryButton=findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryButton.setVisibility(View.GONE);
                getDataTextView.setText(R.string.getting_data);
                getRemoteSettings();
            }
        });
        getRemoteSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initKeyboard();
    }

    @Override
    protected void onPause() {
        endSession(true);
        super.onPause();
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sounds=sharedPreferences.getBoolean("click_sound",false);
        soundsVol=sharedPreferences.getInt("click_volume",0)/100.f;
    }

    private void getRemoteSettings() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                try {
                    String rstring=gson.toJson(new RemotePreferences(1,1,true,1));
                    Log.i(TAG,rstring);
                    SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String serverUrl = sharedPreferences.getString("remote_url","");
                    if(serverUrl.charAt(serverUrl.length()-1)!='/')
                        serverUrl=serverUrl+'/';
                    String wordlistsUrl=serverUrl+WORDLIST_REMOTE_JSON_FILE;
                    Log.i(TAG,"getting wordlists from" + wordlistsUrl);
                    wordLists = gson.fromJson(new InputStreamReader(new URL(wordlistsUrl).openStream()), WordLists.class);
                    Log.i(TAG,"updated wordlists from" + wordlistsUrl);
                    String settingsUrl = serverUrl+SETTINGS_REMOTE_JSON_FILE;
                    Log.i(TAG,"getting remote settings from" + settingsUrl);
                    remotePreferences=gson.fromJson(new InputStreamReader(new URL(settingsUrl).openStream()), RemotePreferences.class);
                    Log.i(TAG,"updated remote settings from" + settingsUrl);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getDataContainer.setVisibility(View.INVISIBLE);
                            contentContainer.setVisibility(View.VISIBLE);
                            keyboardContainer.setVisibility(View.VISIBLE);
                            initKeyboard();
                            initWordListsSpinner();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getDataTextView.setText(R.string.getting_data_error);
                            retryButton.setVisibility(View.VISIBLE);
                        }
                    });
                    Log.w(TAG,"error updating remote data",e);
                }
            }
        });
    }

    private void initKeyboard() {
        tapTimingKeyboard = new TapTimingKeyboard(getApplicationContext(), TTKeyboardLayout.Layout.SIMPLEST_QWERTY_SYMMETRIC, new TTKeyboardClickListener() {
            @Override
            public void onKeyboardClick(TTKeyboardButton ttButton, long clickId) {
                if(sessionActive)
                    checkKeyboardClick(ttButton, clickId);
            }
        },remotePreferences);
        updateSessionInfo(false);
        ConstraintLayout keyboardContainer = findViewById(R.id.keyboard_container);
        keyboardContainer.removeAllViews();
        keyboardContainer.addView(tapTimingKeyboard.getView());
    }

    private void prepareStartSession() {
        if(listsSpinner.getSelectedItem()==null || !(listsSpinner.getSelectedItem() instanceof WordLists.WordList)) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_wordlist_selected),Toast.LENGTH_LONG).show();
            return;
        }
        final WordLists.WordList wordList = ((WordLists.WordList)listsSpinner.getSelectedItem());
        sessionStartButton.setClickable(false);
        listLinearLayout.setVisibility(View.INVISIBLE);
        buttonsContainer.setVisibility(View.GONE);
        Boolean isLandscape=false;
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE)
            isLandscape=true;
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_UNDEFINED)
            isLandscape=null;
        final Boolean isOrientationLandscape=isLandscape;
        final String phoneInfo = Build.BRAND+","+Build.MODEL+","+Build.PRODUCT+","+Build.DEVICE;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TestSession testSession = new TestSession(tapTimingKeyboard.getUserId(),System.currentTimeMillis(),wordList.getName(),wordList.getWordsCsv().hashCode(),isOrientationLandscape,phoneInfo);
                sessionId=TapTimingDatabase.instance(getApplicationContext()).testSessionDao().insert(testSession);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startSession(wordList);
                    }
                });
            }
        });
    }

    private void startSession(WordLists.WordList wordList) {
        sessionActive=true;
        tapTimingKeyboard.startTestSession(sessionId);
        updateSessionInfo(true);
        words=wordList.getWordsCsv().split(",");
        wordsIterator=0;
        currentWord=words[0].toCharArray();
        charsIterator=0;
        currentChar=currentWord[0];
        clicksIds.clear();
        loadWord();
    }

    private void endSession(boolean aborted) {
        sessionActive=false;
        tapTimingKeyboard.endTestSession();
        updateSessionInfo(false);
        sessionStartButton.setClickable(true);
        buttonsContainer.setVisibility(View.VISIBLE);
        listLinearLayout.setVisibility(View.VISIBLE);
        testWordTextView.setText("");
        clicksIds.clear();
        if (!aborted) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    long timestampMs=System.currentTimeMillis();
                    TestSession testSession = TapTimingDatabase.instance(getApplicationContext()).testSessionDao().getById(sessionId);
                    testSession.setSessionEndTimestampMs(timestampMs);
                    TapTimingDatabase.instance(getApplicationContext()).testSessionDao().update(testSession);
                }
            });
        }
    }

    private void updateSessionInfo(boolean sessionActive) {
        if(sessionActive)
            sessionInfoTextView.setText(String.format(getResources().getString(R.string.session_info_active),tapTimingKeyboard.getUserId(),sessionId));
        else
            sessionInfoTextView.setText(String.format(getResources().getString(R.string.session_info_inactive),tapTimingKeyboard.getUserId()));
    }

    private boolean nextWord() {
        if(++wordsIterator>=words.length)
            return false;
        currentWord=words[wordsIterator].toCharArray();
        charsIterator=0;
        currentChar=currentWord[0];
        loadWord();
        return true;
    }

    private boolean nextChar() {
        if (++charsIterator>=currentWord.length)
            return false;
        currentChar=currentWord[charsIterator];
        return true;
    }

    private void loadWord() {
        testWordTextView.setText(words[wordsIterator]);
    }

    private void resetWord() {
        charsIterator=0;
        currentChar=currentWord[0];
    }

    private void acceptWaitingClicks() {
        Iterator<Long> iterator = clicksIds.iterator();
        while (iterator.hasNext()) {
            tapTimingKeyboard.acceptButtonClick(iterator.next());
            iterator.remove();
        }
    }
    private void rejectWaitingClicks() {
        Iterator<Long> iterator = clicksIds.iterator();
        while (iterator.hasNext()) {
            tapTimingKeyboard.rejectButtonClick(iterator.next());
            iterator.remove();
        }
    }

    private void checkKeyboardClick(TTKeyboardButton ttButton, long clickId) {
        clicksIds.add(clickId);
        if(ttButton.getCode()==currentChar) {   //correct keyboard click
            if(!nextChar()) {   //end of word, word correctly typed
                acceptWaitingClicks();
                tapTimingKeyboard.abortCurrentFlightTime();
                if(!nextWord()) { //end of session
                    endSession(false);
                }
            }
        } else {
            if(charsIterator>0) {
                resetWord();
                //checkKeyboardClick(ttButton, clickId);
            }
            testWordBlink();
            if (sounds)
                errorSound();
            rejectWaitingClicks();
            tapTimingKeyboard.abortCurrentFlightTime();
        }
    }

    private void startButtonClick(View view) {
        confirmStart();
    }

    private void confirmStart() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.start_session_confirmation_title))
                .setMessage(getResources().getString(R.string.start_session_confirmation_text))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        prepareStartSession();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void initWordListsSpinner() {
        ArrayList<WordLists.WordList> lists = new ArrayList<>();
        Iterator<WordLists.WordList> it = wordLists.getLists().iterator();
        while(it.hasNext())
            lists.add(it.next());
        listsSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,lists));
    }

    private void testWordBlink() {
        testWordTextView.setTextColor(ResourcesCompat.getColor(getResources(),R.color.colorTestWordError,null));
        if(testWordColorFuture!=null && !testWordColorFuture.isDone())
            testWordColorFuture.cancel(true);
        testWordColorFuture=scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        testWordTextView.setTextColor(ResourcesCompat.getColor(getResources(),R.color.colorTestWord,null));
                    }
                });
            }
        },TEST_WORD_BLINK_TIME_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void errorSound() {
        if (audioManager == null)
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID,soundsVol);
    }


}
