package com.example.taptimingkeyboard;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TapTimingKeyboardService extends InputMethodService {

    public static final String TAG = TapTimingKeyboardService.class.getName();

    private TapTimingKeyboard tapTimingKeyboard;

    //which buttons are currently pressed (but not released) and associated MotionEvents
    private Map<TTKeyboardButton,KeyDownParameters> ttButtonsDownParametersMap = Collections.synchronizedMap(new HashMap<TTKeyboardButton, KeyDownParameters>());
    private TTKeyboardButton lastTTButtonDown = null;
    private TTKeyboardButton lastCommittedTTButton = null;
    private long lastTTButtonCommitTimeMillis = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        tapTimingKeyboard = new TapTimingKeyboard(this, TTKeyboardLayout.Layout.SIMPLEST_QWERTY_SYMMETRIC, new TTKeyboardMotionEventListener() {
            @Override
            public void onMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {
                handleMotionEvent(ttButton,motionEvent);
            }
        });
    }

    @Override
    public View onCreateInputView() {
        Log.d(TAG,"onCreateInputView");
        return tapTimingKeyboard.getView();
    }

    private void handleMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG, ttButton.getLabel() + " ACTION_DOWN");
                KeyDownParameters keyDownParameters = new KeyDownParameters(motionEvent.getEventTime(),motionEvent.getPressure(),motionEvent.getX(),motionEvent.getY());
                if(!ttButtonsDownParametersMap.isEmpty()) {
                    //TODO distance
                    new FlightTimeCharacteristics(lastCommittedTTButton,lastTTButtonDown,0,0);
                    getCurrentInputConnection().commitText(""+(char)lastTTButtonDown.getCode(),1);
                    Log.d(TAG,"zero flight time: "+lastCommittedTTButton.getLabel() + "->" + lastTTButtonDown.getLabel());
                    ttButtonsDownParametersMap.get(lastTTButtonDown).setCommitted(true);
                    lastCommittedTTButton=lastTTButtonDown;
                    lastTTButtonCommitTimeMillis = motionEvent.getEventTime();
                }
                ttButtonsDownParametersMap.put(ttButton,keyDownParameters);
                lastTTButtonDown=ttButton;
                break;
            case MotionEvent.ACTION_UP:
                Log.v(TAG, ttButton.getLabel() + " ACTION_UP");
                if(!ttButtonsDownParametersMap.containsKey(ttButton))
                    return;
                KeyDownParameters correspondingKeyDownParameters = ttButtonsDownParametersMap.get(ttButton);
                if(lastTTButtonDown==ttButton && !correspondingKeyDownParameters.isCommitted()) {
                    if(lastCommittedTTButton != null) {
                        new FlightTimeCharacteristics(lastCommittedTTButton,ttButton,0,correspondingKeyDownParameters.getTimeMillis()-lastTTButtonCommitTimeMillis);
                        Log.d(TAG,"flight time (millis): "+ lastCommittedTTButton.getLabel() + "->" + ttButton.getLabel()+": "+(correspondingKeyDownParameters.getTimeMillis()-lastTTButtonCommitTimeMillis));
                    }
                    getCurrentInputConnection().commitText("" + (char) ttButton.getCode(), 1);
                    lastCommittedTTButton = ttButton;
                    lastTTButtonCommitTimeMillis = motionEvent.getEventTime();
                }
                long holdTimeMillis = motionEvent.getEventTime() - correspondingKeyDownParameters.getTimeMillis();
                KeyTapCharacteristics keyTapCharacteristics = new KeyTapCharacteristics(ttButton,holdTimeMillis,correspondingKeyDownParameters.getPressure());
                Log.d(TAG,"tapped button: " + ttButton.getLabel() + " hold time (millis): " + holdTimeMillis + " pressure: " + correspondingKeyDownParameters.getPressure());
                ttButtonsDownParametersMap.remove(ttButton);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.v(TAG, ttButton.getLabel() + " ACTION_MOVE");
                break;
        }
    }
}
