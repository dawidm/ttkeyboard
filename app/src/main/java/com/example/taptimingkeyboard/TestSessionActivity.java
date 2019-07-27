package com.example.taptimingkeyboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

public class TestSessionActivity extends AppCompatActivity {

    private TapTimingKeyboard tapTimingKeyboard;

    private ArrayList<Long> clicksIds = new ArrayList<>();
    private String[] words;
    private int wordsIterator;
    private char[] currentWord;
    private int charsIterator;
    private char currentChar = 0;

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

    private void startSession() {
        tapTimingKeyboard.startTestSession();
        updateSessionInfo(true);
        sessionStartButton.setClickable(false);
        sessionStopButton.setClickable(true);
        //TODO generate session id and save timestamp
        words=getResources().getStringArray(R.array.test_words);
        wordsIterator=0;
        currentWord=words[0].toCharArray();
        charsIterator=0;
        currentChar=currentWord[0];
        clicksIds.clear();
        loadWord();
    }

    private void endSession(boolean aborted) {
        tapTimingKeyboard.stopTestSession();
        updateSessionInfo(false);
        sessionStartButton.setClickable(true);
        sessionStopButton.setClickable(false);
        testWordTextView.setText("");
        clicksIds.clear();
        if (!aborted) {
            //TODO save session info
        }
    }

    private void updateSessionInfo(boolean sessionActive) {
        if(sessionActive)
            sessionInfoTextView.setText(String.format("User id: %s, session %s",tapTimingKeyboard.getUserId(),1));
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
            //TODO session id
            tapTimingKeyboard.acceptButtonClick(iterator.next(),1);
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
        if(ttButton.getCode()==currentChar) {   //correct keyboard click
            clicksIds.add(clickId);
            if(!nextChar()) {   //end of word, word correctly typed
                acceptWaitingClicks();
                if(!nextWord()) { //end of session
                    endSession(false);
                }
            }
        } else {
            if(charsIterator>0) {
                resetWord();
                rejectWaitingClicks();
                checkKeyboardClick(ttButton, clickId);
            }
        }
    }

    private void startButtonClick(View view) {
        startSession();
    }

    private void stopButtonClick(View view) {
        endSession(true);
    }


}
