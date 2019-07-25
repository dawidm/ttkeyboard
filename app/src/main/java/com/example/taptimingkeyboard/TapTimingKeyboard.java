package com.example.taptimingkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TapTimingKeyboard implements TTKeyboardMotionEventListener {

    public static final String TAG = TapTimingKeyboard.class.getName();

    private View tapTimingKeyboardView;
    private TTKeyboardLayout ttKeyboardLayout;
    private TTKeyboardClickListener clickListener;
    private Map<TTKeyboardButton,Button> buttonsMap = new HashMap<>();

    private double pixelSizeMmX;
    private double pixelSizeMmY;

    //which buttons are currently pressed (but not released) and associated MotionEvents
    private Map<TTKeyboardButton,KeyDownParameters> ttButtonsDownParametersMap = Collections.synchronizedMap(new HashMap<TTKeyboardButton, KeyDownParameters>());
    private TTKeyboardButton lastTTButtonDown = null;
    private TTKeyboardButton lastCommittedTTButton = null;
    private long lastTTButtonCommitTimeMillis = 0;

    public TapTimingKeyboard(Context context, TTKeyboardLayout.Layout layout, TTKeyboardClickListener clickListener) {
        this.clickListener=clickListener;
        Point point = new Point();
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        Log.i(TAG,"screen size (px): "+point.x+"x"+point.y);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        double longerScreenDimension = sharedPreferences.getFloat("longer_screen_dimension",0.0f);
        double shorterScreenDimension = sharedPreferences.getFloat("shorter_screen_dimension",0.0f);
        pixelSizeMmX = (point.x>point.y) ? (longerScreenDimension/point.x) : (shorterScreenDimension/point.x);
        pixelSizeMmY = (point.y>point.x) ? (longerScreenDimension/point.y) : (shorterScreenDimension/point.y);
        Log.i(TAG, "pixel size x (mm) = " + pixelSizeMmX + " pixel size  (mm) = " + pixelSizeMmY);
        ttKeyboardLayout=TTKeyboardLayout.withLayout(layout);
        tapTimingKeyboardView = createView(context);
    }

    public View getView() {
        return tapTimingKeyboardView;
    }

    public TTKeyboardLayout getLayout() {
        return ttKeyboardLayout;
    }

    public double getButtonDistancePixels(TTKeyboardButton from, TTKeyboardButton to) {
        if(buttonsMap.isEmpty())
            throw new IllegalStateException("called getButtonDistance before creating layout view");
        Button buttonFrom = buttonsMap.get(from);
        Button buttonTo = buttonsMap.get(to);
        double xFrom = buttonFrom.getX();
        double xTo = buttonTo.getX();
        double yFrom = buttonFrom.getY();
        double yTo = buttonTo.getY();
        return Math.sqrt(Math.pow(Math.abs(xFrom-xTo),2)+Math.pow(Math.abs(yFrom-yTo),2));
    }

    public double getButtonDistanceMillimeters(TTKeyboardButton from, TTKeyboardButton to) {
        if(buttonsMap.isEmpty())
            throw new IllegalStateException("called getButtonDistance before creating layout view");
        Button buttonFrom = buttonsMap.get(from);
        Button buttonTo = buttonsMap.get(to);
        double xFromMm = buttonFrom.getX()*pixelSizeMmX;
        double xToMm = buttonTo.getX()*pixelSizeMmX;
        double yFromMm = buttonFrom.getY()*pixelSizeMmY;
        double yToMm = buttonTo.getY()*pixelSizeMmY;
        return Math.sqrt(Math.pow(Math.abs(xFromMm-xToMm),2)+Math.pow(Math.abs(yFromMm-yToMm),2));
    }

    private View createView(Context context) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        for(Iterator<TTKeyboardRow> itRow = ttKeyboardLayout.getRows().iterator(); itRow.hasNext();) {
            TTKeyboardRow row = itRow.next();
            LinearLayout rowLinearLayout = new LinearLayout(context);
            for(Iterator<TTKeyboardElement> itElement = row.getElements().iterator(); itElement.hasNext();) {
                TTKeyboardElement element = itElement.next();
                if(element instanceof TTKeyboardButton) {
                    final TTKeyboardButton ttButton = (TTKeyboardButton)element;
                    Button button = new Button(context);
                    button.setText(ttButton.getLabel());
                    button.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,ttButton.getSize()));
                    button.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            Log.v(TAG,ttButton.getLabel()+" onTouch called: "+ motionEvent.getAction() + " X: " + motionEvent.getX() + "Y: " + motionEvent.getY());
                            onMotionEvent(ttButton,motionEvent);
                            return false;
                        }
                    });
                    Log.v(TAG,"adding button " + ttButton.getLabel() + " size " +ttButton.getSize());
                    rowLinearLayout.addView(button);
                    buttonsMap.put(ttButton,button);
                }
                if(element instanceof TTKeyboardSpacer) {
                    TTKeyboardSpacer ttSpacer = (TTKeyboardSpacer)element;
                    Space space = new Space(context);
                    space.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,ttSpacer.getSize()));
                    Log.v(TAG,"adding space size " + ttSpacer.getSize());
                    rowLinearLayout.addView(space);
                }
            }
            mainLayout.addView(rowLinearLayout);
        }
        return mainLayout;
    }

    @Override
    public void onMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG, ttButton.getLabel() + " ACTION_DOWN");
                KeyDownParameters keyDownParameters = new KeyDownParameters(motionEvent.getEventTime(),motionEvent.getPressure(),motionEvent.getX(),motionEvent.getY());
                if(!ttButtonsDownParametersMap.isEmpty()) {
                    new FlightTimeCharacteristics(lastCommittedTTButton,
                            lastTTButtonDown,
                            getButtonDistanceMillimeters(lastCommittedTTButton,lastTTButtonDown),
                            0);
                    clickListener.onKeyboardClick(lastTTButtonDown);
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
                        new FlightTimeCharacteristics(lastCommittedTTButton,ttButton,getButtonDistanceMillimeters(lastCommittedTTButton,ttButton),correspondingKeyDownParameters.getTimeMillis()-lastTTButtonCommitTimeMillis);
                        Log.d(TAG,"flight time (millis): "+ lastCommittedTTButton.getLabel() + "->" + ttButton.getLabel()+": "+(correspondingKeyDownParameters.getTimeMillis()-lastTTButtonCommitTimeMillis));
                    }
                    clickListener.onKeyboardClick(ttButton);
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
