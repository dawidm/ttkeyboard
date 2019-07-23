package com.example.taptimingkeyboard;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TapTimingKeyboardService extends InputMethodService {

    public static final String TAG = TapTimingKeyboardService.class.getName();

    private TapTimingKeyboard tapTimingKeyboard;

    private MotionEvent lastActionDownMotionEvent = null;
    private long lastActionDownTimeNanos = 0;
    private TTKeyboardButton lastTTButton = null;
    private long lastCharacterActionUpTimeNanos = 0;

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
                lastActionDownMotionEvent=motionEvent;
                lastActionDownTimeNanos=System.nanoTime();
                break;
            case MotionEvent.ACTION_UP:
                long nanoTimeSnapshot = System.nanoTime();
                Log.v(TAG, ttButton.getLabel() + " ACTION_UP");
                if(lastActionDownMotionEvent==null || lastActionDownTimeNanos==0)
                    return;
                getCurrentInputConnection().commitText(""+(char)ttButton.getCode(),1);
                long holdTimeNanos = nanoTimeSnapshot-lastActionDownTimeNanos;
                KeyTapCharacteristics keyTapCharacteristics = new KeyTapCharacteristics(ttButton,holdTimeNanos,lastActionDownMotionEvent.getPressure());
                Log.d(TAG,"tapped button:L " + ttButton.getLabel() + " hold time (nanos): " + holdTimeNanos + " pressure: " + lastActionDownMotionEvent.getPressure());
                //flight time only between letter characters
                if(lastTTButton!=null && lastTTButton.isLetterCharacter() && ttButton.isLetterCharacter()) {
                    long flightTimeNanos=lastActionDownTimeNanos- lastCharacterActionUpTimeNanos;
                    Log.d(TAG, "flight time (nanos): " + flightTimeNanos);
                }
                lastTTButton=ttButton;
                lastCharacterActionUpTimeNanos = nanoTimeSnapshot;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.v(TAG, ttButton.getLabel() + " ACTION_MOVE");
                //means that user has swiped, don't handle current touch event as click
                lastActionDownMotionEvent=null;
                break;
        }
    }
}
