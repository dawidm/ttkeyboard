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
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taptimingkeyboard.R;
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
    private static final float ERROR_SOUND_VOLUME = 0.5f;

    private TapTimingKeyboard tapTimingKeyboard;
    private AudioManager audioManager;

    private long sessionId;
    private boolean sessionActive=false;
    private ArrayList<Long> clicksIds = new ArrayList<>();
    private String[] words;
    private int wordsIterator;
    private char[] currentWord;
    private int charsIterator;
    private char currentChar = 0;

    private TextView testWordTextView;
    private TextView sessionInfoTextView;
    private Button sessionStartButton;
    private LinearLayout buttonsContainer;
    private Spinner listsSpinner;
    private LinearLayout listLinearLayout;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture testWordColorFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_session);
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
        emptySpinnerArray.add("(no word lists)");
        listsSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,emptySpinnerArray));
        listLinearLayout=findViewById(R.id.lists_linear_layout);
        loadWordLists();
        initKeyboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_test_session, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_update:
                updateWordLists();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void initKeyboard() {
        tapTimingKeyboard = new TapTimingKeyboard(getApplicationContext(), TTKeyboardLayout.Layout.SIMPLEST_QWERTY_SYMMETRIC, new TTKeyboardClickListener() {
            @Override
            public void onKeyboardClick(TTKeyboardButton ttButton, long clickId) {
                if(sessionActive)
                    checkKeyboardClick(ttButton, clickId);
            }
        });
        updateSessionInfo(false);
        ConstraintLayout keyboardContainer = findViewById(R.id.keyboard_container);
        keyboardContainer.removeAllViews();
        keyboardContainer.addView(tapTimingKeyboard.getView());
    }

    private void prepareStartSession() {
        if(listsSpinner.getSelectedItem()==null || !(listsSpinner.getSelectedItem() instanceof WordLists.WordList)) {
            Toast.makeText(getApplicationContext(),"no wordlist selected",Toast.LENGTH_LONG).show();
            return;
        }
        final WordLists.WordList wordList = ((WordLists.WordList)listsSpinner.getSelectedItem());
        sessionStartButton.setClickable(false);
        listLinearLayout.setVisibility(View.INVISIBLE);
        buttonsContainer.setVisibility(View.GONE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TestSession testSession = new TestSession(tapTimingKeyboard.getUserId(),System.currentTimeMillis(),wordList.getName(),wordList.getWordsCsv().hashCode());
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
            sessionInfoTextView.setText(String.format("User id: %s, session %s",tapTimingKeyboard.getUserId(),sessionId));
        else
            sessionInfoTextView.setText(String.format("User id: %s",tapTimingKeyboard.getUserId()));
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
                .setTitle("Rozpoczęcie sesji")
                .setMessage("Czy na pewno chcesz rozpocząć sesję testową?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        prepareStartSession();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void updateWordLists() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                try {
                    SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String url = sharedPreferences.getString("wordlists_url","");
                    WordLists wordLists = gson.fromJson(new InputStreamReader(new URL(url).openStream()), WordLists.class);
                    sharedPreferences.edit().putString("wordlistsJson",gson.toJson(wordLists)).commit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"updated wordlists",Toast.LENGTH_LONG).show();
                            loadWordLists();
                        }
                    });
                    Log.i(TAG,"updated wordlists from" + url);
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"error updating wordlists",Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.w(TAG,"error updating wordlists",e);
                }
            }
        });
    }

    private void loadWordLists() {
        String wordlistJson = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("wordlistsJson","");
        if(wordlistJson=="") {
            return;
        }
        WordLists wordLists = new Gson().fromJson(wordlistJson, WordLists.class);
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
        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID,ERROR_SOUND_VOLUME);
    }


}
