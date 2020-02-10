package com.example.taptimingkeyboard.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.data.Md5Hash;
import com.example.taptimingkeyboard.data.RemotePreferences;
import com.example.taptimingkeyboard.data.TestSessionWordErrors;
import com.example.taptimingkeyboard.data.UserInfo;
import com.example.taptimingkeyboard.data.firebase.FirebaseSessionSync;
import com.example.taptimingkeyboard.keyboard.TTKeyboardButton;
import com.example.taptimingkeyboard.keyboard.TTKeyboardClickListener;
import com.example.taptimingkeyboard.keyboard.TTKeyboardLayout;
import com.example.taptimingkeyboard.keyboard.TapTimingKeyboard;
import com.example.taptimingkeyboard.data.WordLists;
import com.example.taptimingkeyboard.data.TapTimingDatabase;
import com.example.taptimingkeyboard.data.TestSession;
import com.example.taptimingkeyboard.tools.LimitedFrequencyExecutor;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An activity for performing test sessions
 */
public class TestSessionActivity extends AppCompatActivity {

    public static final String TAG = TestSessionActivity.class.getName();

    public static final int TEST_WORD_BLINK_TIME_MILLIS = 1000;
    private static final int ERROR_TIMEOUT_MILLIS = 1000;
    public static final int ENABLE_SESSION_END_OK_BUTTON_DELAY_MILLIS = 10000;

    private AtomicBoolean settingsInitialized = new AtomicBoolean(false);

    private TapTimingKeyboard tapTimingKeyboard;
    private UiSounds uiSounds;
    private RemoteSettingsLoader remoteSettingsLoader;
    private FirebaseSessionSync firebaseSessionSync;

    private LimitedFrequencyExecutor limitedFrequencyExecutor=new LimitedFrequencyExecutor();
    private static final int COUNT_ERROR_TASK_ID=1;

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
    private boolean showTypingProgress;
    private boolean randomizeWordOrder=false;

    private TextView testWordTextView;
    private TextView testWordCorrectTextView;
    private TextView sessionInfoTextView;
    private Button sessionStartButton;
    private LinearLayout buttonsContainer;
    private Spinner listsSpinner;
    private LinearLayout listLinearLayout;
    private LinearLayout contentContainer;
    private ConstraintLayout keyboardContainer;
    private ConstraintLayout getDataContainer;
    private ConstraintLayout sessionEndContainer;
    private TextView getDataTextView;
    private Button retryButton;
    private Button sessionEndOkButton;
    private ProgressBar progressBar;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture testWordColorFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_session);
        userId=getIntent().getExtras().getLong("user_id");
        testWordTextView = findViewById(R.id.test_word_textview);
        testWordCorrectTextView = findViewById(R.id.test_word_correct_chars_textview);
        sessionInfoTextView = findViewById(R.id.session_info_textview);
        sessionStartButton = findViewById(R.id.start_button);
        buttonsContainer=findViewById(R.id.buttons_container);
        listsSpinner=findViewById(R.id.lists_spinner);
        listLinearLayout=findViewById(R.id.lists_linear_layout);
        contentContainer=findViewById(R.id.content_container);
        keyboardContainer=findViewById(R.id.keyboard_container);
        getDataContainer=findViewById(R.id.get_data_container);
        sessionEndContainer=findViewById(R.id.session_end_container);
        getDataTextView=findViewById(R.id.get_data_text_view);
        retryButton=findViewById(R.id.retry_button);
        sessionEndOkButton=findViewById(R.id.session_end_ok_button);
        progressBar=findViewById(R.id.progressBar);
        uiSounds = new UiSounds(this);
        firebaseSessionSync = new FirebaseSessionSync(getApplicationContext());
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
        sessionEndOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionEndContainer.setVisibility(View.INVISIBLE);
            }
        });
        ArrayList<String> emptySpinnerArray = new ArrayList<>(1);
        emptySpinnerArray.add(getResources().getString(R.string.wordlist_spinner_empty));
        listsSpinner.setAdapter(new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,emptySpinnerArray));
        getDataContainer.bringToFront();
        sessionEndContainer.bringToFront();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(getString(R.string.text_logout));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, FireBaseLoginActivity.class);
                startActivity(intent);
                finish();
            break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
        initKeyboard();
    }

    @Override
    protected void onPause() {
        if(sessionActive)
            endSession(true);
        super.onPause();
    }

    /**
     * Use remote and local settings to init UI
     */
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

    /**
     * Populate word list spinner with word lists names
     */
    private void initWordListsSpinner() {
        ArrayList<WordLists.WordList> lists = new ArrayList<>();
        Iterator<WordLists.WordList> it = wordLists.getLists().iterator();
        while(it.hasNext())
            lists.add(it.next());
        listsSpinner.setAdapter(new ArrayAdapter<>(TestSessionActivity.this,R.layout.support_simple_spinner_dropdown_item,lists));
    }

    /**
     * Set settings variables using remote settings values (if available) or application's SharedPreferences
     */
    private void loadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sounds=(remotePreferences!=null&&remotePreferences.getSound()!=null)?remotePreferences.getSound():sharedPreferences.getBoolean("click_sound",true);
        soundsVol=(remotePreferences!=null&&remotePreferences.getVolume()!=null)?remotePreferences.getVolume()/100.f:sharedPreferences.getInt("click_volume",0)/100.f;
        vibrations=(remotePreferences!=null&&remotePreferences.getVibrations()!=null)?remotePreferences.getVibrations():sharedPreferences.getBoolean("vibrations",false);
        showTypingProgress=sharedPreferences.getBoolean("typing_progress",false);
        if(remotePreferences!=null&&remotePreferences.getRandomizeWordOrder()!=null)
            randomizeWordOrder=remotePreferences.getRandomizeWordOrder();
        if(sounds)
            uiSounds.initSounds();
    }

    /**
     * Instantiate TapTimingKeyboard (choose layout, set click listener, pass RemotePreferences and user id) and add it's View to the UI.
     */
    private void initKeyboard() {
        tapTimingKeyboard = new TapTimingKeyboard(getApplicationContext(), TTKeyboardLayout.Layout.SIMPLEST_QWERTY_SYMMETRIC, new TTKeyboardClickListener() {
            @Override
            public void onKeyboardClick(TTKeyboardButton ttButton, long clickId) {
                if(sessionActive)
                    checkKeyboardClick((char)ttButton.getCode(), clickId);
            }
        },remotePreferences,
        userId);
        ConstraintLayout keyboardContainer = findViewById(R.id.keyboard_container);
        keyboardContainer.removeAllViews();
        keyboardContainer.addView(tapTimingKeyboard.getView());
    }

    /**
     * Asynchronously load user information from the database.
     * @param userId Id of the user to load.
     * @param afterUpdateRunnable What to do after updating.
     */
    private void loadUserName(final long userId, final Runnable afterUpdateRunnable) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                userInfo=TapTimingDatabase.instance(getApplicationContext()).userInfoDao().getById(userId);
                runOnUiThread(afterUpdateRunnable);
            }
        });
    }

    /**
     * Show dialog to confirm starting test session, run {@link #prepareStartSession()}
     */
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

    /**
     * Create instance of {@link TestSession} with required data and save it to the database.
     */
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
        final String phoneInfo = String.format("BRAND:\"%s\",MODEL:\"%s\",PRODUCT:\"%s\",DEVICE:\"%s\"",Build.BRAND,Build.MODEL,Build.PRODUCT,Build.DEVICE);
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TestSession testSession = new TestSession(userId,System.currentTimeMillis(),wordList.getName(),Md5Hash.fromString(wordList.getWordsCsv()),isOrientationLandscape,phoneInfo,displayMetrics.xdpi,displayMetrics.ydpi);
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

    /**
     * Start a new test session.
     * Prepare layout for test session. Set TapTimingKeyboard to test session mode. Init variables associated with test session process.
     * @param wordList Word list to use in the session.
     */
    private void startSession(WordLists.WordList wordList) {
        sessionStartButton.setClickable(false);
        listLinearLayout.setVisibility(View.INVISIBLE);
        buttonsContainer.setVisibility(View.GONE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        sessionActive=true;
        tapTimingKeyboard.startTestSession(sessionId);
        updateSessionInfo();
        words=wordList.getWords();
        if(randomizeWordOrder) {
            List<String> wordsArrayList = Arrays.asList(words);
            Collections.shuffle(wordsArrayList);
            words = wordsArrayList.toArray(new String[wordsArrayList.size()]);
        }
        numErrors=0;
        wordsIterator=0;
        currentWord=words[0].toCharArray();
        charsIterator=0;
        currentChar=currentWord[0];
        clicksIds.clear();
        wordsErrorsMap.clear();
        loadWord();
    }

    /**
     * End test session.
     * Update interface and database entry with session info
     * @param aborted
     */
    private void endSession(final boolean aborted) {
        sessionActive=false;
        tapTimingKeyboard.endTestSession();
        updateSessionInfo();
        sessionStartButton.setClickable(true);
        buttonsContainer.setVisibility(View.VISIBLE);
        listLinearLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        testWordTextView.setText("");
        testWordCorrectTextView.setText("");
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        sessionEndContainer.setVisibility(View.VISIBLE);
        sessionEndOkButton.setText(R.string.sending_results_button);
        sessionEndOkButton.setEnabled(false);
        clicksIds.clear();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enableSessionEndOkButton();
                    }
                });
            }
        }, ENABLE_SESSION_END_OK_BUTTON_DELAY_MILLIS);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                long timestampMs = System.currentTimeMillis();
                final TestSession testSession = TapTimingDatabase.instance(getApplicationContext()).testSessionDao().getById(sessionId);
                if (!aborted)
                    testSession.setSessionEndTimestampMs(timestampMs);
                testSession.setNumErrors(numErrors);
                TapTimingDatabase.instance(getApplicationContext()).testSessionDao().update(testSession);
                TestSessionWordErrors[] testSessionWordErrors = TestSessionWordErrors.fromMap(wordsErrorsMap, sessionId);
                TapTimingDatabase.instance(getApplicationContext()).testSessionWordErrorsDao().insertAll(testSessionWordErrors);
                firebaseSessionSync.syncSession(testSession, testSessionWordErrors, new FirebaseSessionSync.OnSuccessfulSyncListener() {
                    @Override
                    public void onSuccessfulSync() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enableSessionEndOkButton();
                            }
                        });
                    }
                }, new FirebaseSessionSync.OnSyncFailureListener() {
                    @Override
                    public void onSyncFailure(Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enableSessionEndOkButton();
                            }
                        });
                    }
                });
            }
        });
    }

    private void enableSessionEndOkButton() {
        sessionEndOkButton.setText(R.string.ok_button);
        sessionEndOkButton.setEnabled(true);
    }

    /**
     * Update TextView with session info
     */
    private void updateSessionInfo() {
        if(sessionActive)
            sessionInfoTextView.setText(String.format(getResources().getString(R.string.session_info_active),userInfo.toString(),sessionId));
        else
            sessionInfoTextView.setText(String.format(getResources().getString(R.string.session_info_inactive),userInfo.toString()));
    }

    /**
     * Try to load next word
     * @return True if next word exists, false if it doesn't.
     */
    private boolean nextWord() {
        if(++wordsIterator>=words.length)
            return false;
        currentWord=words[wordsIterator].toCharArray();
        charsIterator=0;
        currentChar=currentWord[0];
        loadWord();
        return true;
    }

    /**
     * Go to next character in current word.
     * @return True if next character exists, false if it doesn't.
     */
    private boolean nextChar() {
        if (++charsIterator>=currentWord.length)
            return false;
        if(showTypingProgress)
            loadWord();
        currentChar=currentWord[charsIterator];
        return true;
    }

    private void loadWord() {
        progressBar.setProgress(100*wordsIterator/words.length);
        if(showTypingProgress) {
            testWordCorrectTextView.setText(words[wordsIterator].substring(0,charsIterator));
            testWordTextView.setText(words[wordsIterator]);
        } else
            testWordTextView.setText(words[wordsIterator]);
    }

    /**
     * Reset the progress of typing current word.
     */
    private void resetWord() {
        charsIterator=0;
        currentChar=currentWord[0];
    }

    /**
     * Check if the typed character if correct.
     * Go to next character or next word if correct, notify about error if not correct.
     * @param character The character of the button clicked.
     * @param clickId Id of the click.
     */
    private void checkKeyboardClick(char character, long clickId) {
        clicksIds.add(clickId);
        if(character==currentChar) {   //correct keyboard click
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

    /**
     * For every word click ids are collected until word is typed correctly or wrong character is clicked.
     * If a word is typed correctly all waiting clicks are sent to TapTimingKeyboard as accepted
     * (associated user interaction data will be saved to the database).
     */
    private void acceptWaitingClicks() {
        Iterator<Long> iterator = clicksIds.iterator();
        while (iterator.hasNext()) {
            tapTimingKeyboard.acceptButtonClick(iterator.next());
            iterator.remove();
        }
    }

    /**
     * See {@link #acceptWaitingClicks()}
     * If a mistake occurs when typing a word, all previous correctly typed characters for this word are sent to TapTimingKeyboard as rejected.
     */
    private void rejectWaitingClicks() {
        Iterator<Long> iterator = clicksIds.iterator();
        while (iterator.hasNext()) {
            tapTimingKeyboard.rejectButtonClick(iterator.next());
            iterator.remove();
        }
    }

    /**
     * Count error (but only if specified amount of time {@link #ERROR_TIMEOUT_MILLIS} hasn't passed since previous count)
     * Save information about numbers of errors for specific words in test session.
     */
    private void countError() {
        if(limitedFrequencyExecutor.canRunNow(COUNT_ERROR_TASK_ID)) {
            limitedFrequencyExecutor.run(COUNT_ERROR_TASK_ID, new Runnable() {
                @Override
                public void run() {
                    numErrors++;
                    if(wordsErrorsMap.containsKey(new String(currentWord))) {
                        Integer incrementedInt=wordsErrorsMap.get(new String(currentWord))+1;
                        wordsErrorsMap.put(new String(currentWord),incrementedInt);
                    } else
                        wordsErrorsMap.put(new String(currentWord),1);
                    Log.d(TAG,"counted error for word \"" + new String(currentWord) + "\"");
                }
            },ERROR_TIMEOUT_MILLIS);
        }
    }

    /**
     * Change the color of the test word for specified amount of time after a mistake in typing
     */
    private void testWordBlink() {
        if(showTypingProgress)
            loadWord();
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
