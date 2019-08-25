package com.example.taptimingkeyboard.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.data.RemotePreferences;
import com.example.taptimingkeyboard.data.TestSessionWordErrors;
import com.example.taptimingkeyboard.data.UserInfo;
import com.example.taptimingkeyboard.keyboard.TTKeyboardButton;
import com.example.taptimingkeyboard.keyboard.TTKeyboardClickListener;
import com.example.taptimingkeyboard.keyboard.TTKeyboardLayout;
import com.example.taptimingkeyboard.keyboard.TapTimingKeyboard;
import com.example.taptimingkeyboard.data.WordLists;
import com.example.taptimingkeyboard.data.TapTimingDatabase;
import com.example.taptimingkeyboard.data.TestSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestSessionActivity extends AppCompatActivity {

    public static final String TAG = TestSessionActivity.class.getName();

    public static final int TEST_WORD_BLINK_TIME_MILLIS = 1000;
    private static final int ERROR_TIMEOUT_MILLIS = 1000;

    private AtomicBoolean settingsInitialized = new AtomicBoolean(false);

    private TapTimingKeyboard tapTimingKeyboard;
    private UiSounds uiSounds;
    private RemoteSettingsLoader remoteSettingsLoader;

    private WordLists wordLists;
    private RemotePreferences remotePreferences;
    private long sessionId;
    private Long userId;
    private UserInfo userInfo;
    private boolean sessionActive=false;
    private ArrayList<Long> clicksIds = new ArrayList<>();
    private String[] words;
    private int wordsIterator;
    private char[] currentWord;
    private int charsIterator;
    private char currentChar = 0;
    private int numErrors;
    private Map<String,Integer> wordsErrorsMap = new HashMap<>();

    private boolean sounds;
    private float soundsVol;
    private boolean vibrations;

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

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
    private ScheduledFuture testWordColorFuture;
    private ScheduledFuture errorTimeoutScheduledFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_session);
        getSupportActionBar().hide();
        userId=getIntent().getExtras().getLong("user_id");
        testWordTextView = findViewById(R.id.test_word_textview);
        sessionInfoTextView = findViewById(R.id.session_info_textview);
        sessionStartButton = findViewById(R.id.start_button);
        buttonsContainer=findViewById(R.id.buttons_container);
        listsSpinner=findViewById(R.id.lists_spinner);
        listLinearLayout=findViewById(R.id.lists_linear_layout);
        contentContainer=findViewById(R.id.content_container);
        keyboardContainer=findViewById(R.id.keyboard_container);
        getDataContainer=findViewById(R.id.get_data_container);
        getDataTextView=findViewById(R.id.get_data_text_view);
        retryButton=findViewById(R.id.retry_button);
        uiSounds = new UiSounds(this);
        remoteSettingsLoader=new RemoteSettingsLoader(getApplicationContext());
        remoteSettingsLoader.subscribeOnSuccessfulLoad(new RemoteSettingsLoader.SuccessfulLoadListener() {
            @Override
            public void onSettingsLoaded(RemotePreferences remotePreferences, WordLists wordLists) {
                TestSessionActivity.this.remotePreferences=remotePreferences;
                TestSessionActivity.this.wordLists=wordLists;
                settingsInitialized.set(true);
                loadPreferences();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        useSettings();
                    }
                });
            }
        });
        remoteSettingsLoader.subscribeOnFailure(new RemoteSettingsLoader.FailureListener() {
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDataTextView.setText(R.string.getting_data_error);
                        retryButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        sessionStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmStart();
            }
        });
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryButton.setVisibility(View.GONE);
                getDataTextView.setText(R.string.getting_data);
                remoteSettingsLoader.loadAsync();
            }
        });
        testWordTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                endSession(true);
                return true;
            }
        });
        ArrayList<String> emptySpinnerArray = new ArrayList<>(1);
        emptySpinnerArray.add(getResources().getString(R.string.wordlist_spinner_empty));
        listsSpinner.setAdapter(new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,emptySpinnerArray));
        if(settingsInitialized.get())
            useSettings();
        else
            remoteSettingsLoader.loadAsync();
        loadUserName(userId, new Runnable() {
            @Override
            public void run() {
                updateSessionInfo();
            }
        });
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

    private void useSettings() {
        if(remotePreferences.getOrientation()!=null) {
            if(remotePreferences.getOrientation()==RemotePreferences.ORIENTATION_PORTRAIT)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if(remotePreferences.getOrientation()==RemotePreferences.ORIENTATION_LANDSCAPE)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        getDataContainer.setVisibility(View.INVISIBLE);
        contentContainer.setVisibility(View.VISIBLE);
        keyboardContainer.setVisibility(View.VISIBLE);
        initKeyboard();
        initWordListsSpinner();
    }

    private void initWordListsSpinner() {
        ArrayList<WordLists.WordList> lists = new ArrayList<>();
        Iterator<WordLists.WordList> it = wordLists.getLists().iterator();
        while(it.hasNext())
            lists.add(it.next());
        listsSpinner.setAdapter(new ArrayAdapter<>(TestSessionActivity.this,R.layout.support_simple_spinner_dropdown_item,lists));
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sounds=(remotePreferences!=null&&remotePreferences.getSound()!=null)?remotePreferences.getSound():sharedPreferences.getBoolean("click_sound",true);
        soundsVol=(remotePreferences!=null&&remotePreferences.getVolume()!=null)?remotePreferences.getVolume()/100.f:sharedPreferences.getInt("click_volume",0)/100.f;
        vibrations=(remotePreferences!=null&&remotePreferences.getVibrations()!=null)?remotePreferences.getVibrations():sharedPreferences.getBoolean("vibrations",false);
        if(sounds)
            uiSounds.initSounds();
    }

    private void initKeyboard() {
        tapTimingKeyboard = new TapTimingKeyboard(getApplicationContext(), TTKeyboardLayout.Layout.SIMPLEST_QWERTY_SYMMETRIC, new TTKeyboardClickListener() {
            @Override
            public void onKeyboardClick(TTKeyboardButton ttButton, long clickId) {
                if(sessionActive)
                    checkKeyboardClick(ttButton, clickId);
            }
        },remotePreferences,
        userId);
        ConstraintLayout keyboardContainer = findViewById(R.id.keyboard_container);
        keyboardContainer.removeAllViews();
        keyboardContainer.addView(tapTimingKeyboard.getView());
    }

    private void loadUserName(final long userId, final Runnable afterUpdateRunnable) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                userInfo=TapTimingDatabase.instance(getApplicationContext()).userInfoDao().getById(userId);
                runOnUiThread(afterUpdateRunnable);
            }
        });
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

    private void prepareStartSession() {
        if(listsSpinner.getSelectedItem()==null || !(listsSpinner.getSelectedItem() instanceof WordLists.WordList)) {
            Toast.makeText(this,getResources().getString(R.string.no_wordlist_selected),Toast.LENGTH_LONG).show();
            return;
        }
        final WordLists.WordList wordList = ((WordLists.WordList)listsSpinner.getSelectedItem());
        Boolean isLandscape=false;
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE)
            isLandscape=true;
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_UNDEFINED)
            isLandscape=null;
        final Boolean isOrientationLandscape=isLandscape;
        final String phoneInfo = Build.BRAND+","+Build.MODEL+","+Build.PRODUCT+","+Build.DEVICE;
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TestSession testSession = new TestSession(userId,System.currentTimeMillis(),wordList.getName(),wordList.getWordsCsv().hashCode(),isOrientationLandscape,phoneInfo,displayMetrics.xdpi,displayMetrics.ydpi);
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
        sessionStartButton.setClickable(false);
        listLinearLayout.setVisibility(View.INVISIBLE);
        buttonsContainer.setVisibility(View.GONE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        sessionActive=true;
        tapTimingKeyboard.startTestSession(sessionId);
        updateSessionInfo();
        words=wordList.getWordsCsv().split(",");
        numErrors=0;
        wordsIterator=0;
        currentWord=words[0].toCharArray();
        charsIterator=0;
        currentChar=currentWord[0];
        clicksIds.clear();
        wordsErrorsMap.clear();
        loadWord();
    }

    private void endSession(boolean aborted) {
        sessionActive=false;
        tapTimingKeyboard.endTestSession();
        updateSessionInfo();
        sessionStartButton.setClickable(true);
        buttonsContainer.setVisibility(View.VISIBLE);
        listLinearLayout.setVisibility(View.VISIBLE);
        testWordTextView.setText("");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        clicksIds.clear();
        if (!aborted) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    long timestampMs=System.currentTimeMillis();
                    TestSession testSession = TapTimingDatabase.instance(getApplicationContext()).testSessionDao().getById(sessionId);
                    testSession.setSessionEndTimestampMs(timestampMs);
                    testSession.setNumErrors(numErrors);
                    TapTimingDatabase.instance(getApplicationContext()).testSessionDao().update(testSession);
                    TapTimingDatabase.instance(getApplicationContext()).testSessionWordErrorsDao().insertAll(TestSessionWordErrors.fromMap(wordsErrorsMap,sessionId));
                }
            });
        }
    }

    private void updateSessionInfo() {
        if(sessionActive)
            sessionInfoTextView.setText(String.format(getResources().getString(R.string.session_info_active),userInfo.toString(),sessionId));
        else
            sessionInfoTextView.setText(String.format(getResources().getString(R.string.session_info_inactive),userInfo.toString()));
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
            }
            countError();
            testWordBlink();
            if(sounds)
                uiSounds.playSound(UiSounds.SOUND_WORD_ERROR,soundsVol,ERROR_TIMEOUT_MILLIS);
            if(vibrations)
                uiSounds.vibrate(UiSounds.VIBRATION_WORD_ERROR,ERROR_TIMEOUT_MILLIS);
            rejectWaitingClicks();
            tapTimingKeyboard.abortCurrentFlightTime();
        }
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

    private void countError() {
        if(errorTimeoutScheduledFuture==null || errorTimeoutScheduledFuture.isDone()) {
            numErrors++;
            if(wordsErrorsMap.containsKey(new String(currentWord))) {
                Integer incrementedInt=wordsErrorsMap.get(new String(currentWord))+1;
                wordsErrorsMap.put(new String(currentWord),incrementedInt);
            } else
                wordsErrorsMap.put(new String(currentWord),1);
            errorTimeoutScheduledFuture=scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {

                }
            },ERROR_TIMEOUT_MILLIS,TimeUnit.MILLISECONDS);
        }
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

}
