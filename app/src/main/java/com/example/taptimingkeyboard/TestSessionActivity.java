package com.example.taptimingkeyboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

public class TestSessionActivity extends AppCompatActivity {

    private TapTimingKeyboard tapTimingKeyboard;

    private long sessionId;
    private ArrayList<Long> clicksIds = new ArrayList<>();
    private String[] words;
    private int wordsIterator;
    private char[] currentWord;
    private int charsIterator;
    private char currentChar = 0;
    private int failedTries;

    private TextView testWordTextView;
    private TextView sessionInfoTextView;
    private Button sessionStartButton;
    private Button sessionStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_session);
        testWordTextView = findViewById(R.id.test_word_textview);
        sessionInfoTextView = findViewById(R.id.session_info_textview);
        sessionStartButton = findViewById(R.id.start_button);
        sessionStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButtonClick(view);
            }
        });
        sessionStopButton = findViewById(R.id.stop_button);
        sessionStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopButtonClick(view);
            }
        });
        initKeyboard();
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
                checkKeyboardClick(ttButton, clickId);
            }
        });
        updateSessionInfo(false);
        ConstraintLayout keyboardContainer = findViewById(R.id.keyboard_container);
        keyboardContainer.removeAllViews();
        keyboardContainer.addView(tapTimingKeyboard.getView());
    }

    private void prepareStartSession() {
        sessionStartButton.setClickable(false);
        sessionStopButton.setClickable(true);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //TODO wordlist name and hash
                TestSession testSession = new TestSession(tapTimingKeyboard.getUserId(),System.currentTimeMillis(),"test","test".hashCode());
                sessionId=TapTimingDatabase.instance(getApplicationContext()).testSessionDao().insert(testSession);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startSession();
                    }
                });
            }
        });
    }

    private void startSession() {
        tapTimingKeyboard.startTestSession(sessionId);
        updateSessionInfo(true);
        sessionStartButton.setClickable(false);
        sessionStopButton.setClickable(true);
        words=getResources().getStringArray(R.array.test_words);
        wordsIterator=0;
        currentWord=words[0].toCharArray();
        charsIterator=0;
        currentChar=currentWord[0];
        failedTries=0;
        clicksIds.clear();
        loadWord();
    }

    private void endSession(boolean aborted) {
        tapTimingKeyboard.endTestSession();
        updateSessionInfo(false);
        sessionStartButton.setClickable(true);
        sessionStopButton.setClickable(false);
        testWordTextView.setText("");
        clicksIds.clear();
        if (!aborted) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    long timestampMs=System.currentTimeMillis();
                    TestSession testSession = TapTimingDatabase.instance(getApplicationContext()).testSessionDao().getById(sessionId);
                    testSession.setSessionEndTimestampMs(timestampMs);
                    testSession.setSessionFailedTries(failedTries);
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
                checkKeyboardClick(ttButton, clickId);
            }
            rejectWaitingClicks();
            tapTimingKeyboard.abortCurrentFlightTime();
            failedTries++;
        }
    }

    private void startButtonClick(View view) {
        prepareStartSession();
    }

    private void stopButtonClick(View view) {
        endSession(true);
    }


}
