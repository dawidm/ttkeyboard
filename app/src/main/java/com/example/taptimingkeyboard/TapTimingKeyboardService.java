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
    private TTKeyboardButton lastTTButtonUp = null;
    private long lastTTButtonUpTimeMillis = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        tapTimingKeyboard = new TapTimingKeyboard(this, new TTKeyboardMotionEventListener() {
            @Override
            public void onMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {
                handleMotionEvent(ttButton,motionEvent);
            }
        });
    }

    @Override
    public View onCreateInputView() {
        return tapTimingKeyboard.getView();
    }

    private void handleMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG, ttButton.getLabel() + " ACTION_DOWN");
                if(!ttButtonsDownParametersMap.isEmpty()) {
                    //TODO distance
                    new FlightTimeCharacteristics(lastTTButtonDown,ttButton,0,0);
                    Log.d(TAG,"zero flight time: "+lastTTButtonDown.getLabel() + "->" + ttButton.getLabel());
                    getCurrentInputConnection().commitText(""+(char)lastTTButtonDown.getCode(),1);
                }
                ttButtonsDownParametersMap.put(ttButton,new KeyDownParameters(motionEvent.getEventTime(),motionEvent.getPressure(),motionEvent.getX(),motionEvent.getY()));
                lastTTButtonDown=ttButton;
                break;
            case MotionEvent.ACTION_UP:
                Log.v(TAG, ttButton.getLabel() + " ACTION_UP");
                if(!ttButtonsDownParametersMap.containsKey(ttButton))
                    return;
                //MotionEvent downMotionEvent = ttButtonsDownParametersMap.get(ttButton);
                KeyDownParameters keyDownParameters = ttButtonsDownParametersMap.get(ttButton);
                if(lastTTButtonDown==ttButton) {
                    if(lastTTButtonUp!=null) {
                        new FlightTimeCharacteristics(lastTTButtonUp,ttButton,0,keyDownParameters.getTimeMillis()-lastTTButtonUpTimeMillis);
                        Log.d(TAG,"flight time (millis): "+lastTTButtonUp.getLabel() + "->" + ttButton.getLabel()+": "+(keyDownParameters.getTimeMillis()-lastTTButtonUpTimeMillis));
                    }
                    getCurrentInputConnection().commitText("" + (char) ttButton.getCode(), 1);
                }
                long holdTimeMillis = motionEvent.getEventTime() - keyDownParameters.getTimeMillis();
                KeyTapCharacteristics keyTapCharacteristics = new KeyTapCharacteristics(ttButton,holdTimeMillis,keyDownParameters.getPressure());
                Log.d(TAG,"tapped button: " + ttButton.getLabel() + " hold time (millis): " + holdTimeMillis + " pressure: " + keyDownParameters.getPressure());
                ttButtonsDownParametersMap.remove(ttButton);
                lastTTButtonUp = ttButton;
                lastTTButtonUpTimeMillis = motionEvent.getEventTime();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.v(TAG, ttButton.getLabel() + " ACTION_MOVE");
                break;
        }
    }
}
