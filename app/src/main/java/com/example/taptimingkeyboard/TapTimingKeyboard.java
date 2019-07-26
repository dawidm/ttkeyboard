package com.example.taptimingkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;

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
    private String userId;

    private double pixelSizeMmX;
    private double pixelSizeMmY;
    private float keyboardHeightPixels;

    //which buttons are currently pressed (but not released) and associated MotionEvents
    private Map<TTKeyboardButton,KeyDownParameters> ttButtonsDownParametersMap = Collections.synchronizedMap(new HashMap<TTKeyboardButton, KeyDownParameters>());
    private TTKeyboardButton lastTTButtonDown = null;
    private TTKeyboardButton lastCommittedTTButton = null;
    private long lastTTButtonCommitTimeMillis = 0;

    private boolean testSessionMode = false;
    private long clickId = 0;

    public TapTimingKeyboard(Context context, TTKeyboardLayout.Layout layout, TTKeyboardClickListener clickListener) {
        this.clickListener=clickListener;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        userId=sharedPreferences.getString("user_id","test_user");
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        float screenHeightPixels=point.y;
        display.getRealSize(point);
        Log.i(TAG,"screen size (px): "+point.x+"x"+point.y);
        double longerScreenDimension = Double.parseDouble(sharedPreferences.getString("longer_screen_dimension","0"));
        double shorterScreenDimension = Double.parseDouble(sharedPreferences.getString("shorter_screen_dimension","0"));
        if(longerScreenDimension==0 || shorterScreenDimension==0)
            Toast.makeText(context,"warning: no screen size provided in settings",Toast.LENGTH_SHORT).show();
        else {
            pixelSizeMmX = (point.x > point.y) ? (longerScreenDimension / point.x) : (shorterScreenDimension / point.x);
            pixelSizeMmY = (point.y > point.x) ? (longerScreenDimension / point.y) : (shorterScreenDimension / point.y);
            Log.i(TAG, "pixel size x (mm) = " + pixelSizeMmX + " pixel size  (mm) = " + pixelSizeMmY);
        }
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            keyboardHeightPixels=sharedPreferences.getFloat("height_landscape",0.4f)*screenHeightPixels;
        else
            keyboardHeightPixels=sharedPreferences.getFloat("height_portrait",0.35f)*screenHeightPixels;
        ttKeyboardLayout=TTKeyboardLayout.withLayout(layout);
        tapTimingKeyboardView = createView(context);
    }

    public void testSessionMode() {
        testSessionMode=true;
    }

    public void acceptButtonClick(long clickId, int sessionId) {

    }

    public void rejectButtonClick(long clickId) {

    }

    public View getView() {
        return tapTimingKeyboardView;
    }

    public TTKeyboardLayout getLayout() {
        return ttKeyboardLayout;
    }

    public double getButtonDistanceMillimeters(TTKeyboardButton from, TTKeyboardButton to) {
        if(buttonsMap.isEmpty())
            throw new IllegalStateException("called getButtonDistance before creating layout view");
        Button buttonFrom = buttonsMap.get(from);
        Button buttonTo = buttonsMap.get(to);
        Rect buttonFromRect = new Rect();
        Rect buttonToRect = new Rect();
        buttonFrom.getDrawingRect(buttonFromRect);
        ((LinearLayout)tapTimingKeyboardView).offsetDescendantRectToMyCoords(buttonFrom,buttonFromRect);
        double xFrom=buttonFromRect.left;
        double yFrom=buttonFromRect.top;
        buttonTo.getDrawingRect(buttonToRect);
        ((LinearLayout)tapTimingKeyboardView).offsetDescendantRectToMyCoords(buttonTo,buttonToRect);
        double xTo=buttonToRect.left;
        double yTo=buttonToRect.top;
        double xFromMm = xFrom*pixelSizeMmX;
        double xToMm = xTo*pixelSizeMmX;
        double yFromMm = yFrom*pixelSizeMmY;
        double yToMm = yTo*pixelSizeMmY;
        return Math.sqrt(Math.pow(Math.abs(xFromMm-xToMm),2)+Math.pow(Math.abs(yFromMm-yToMm),2));
    }

    private View createView(Context context) {
        float rowHeightPixels = keyboardHeightPixels/ttKeyboardLayout.getRows().size();
        LinearLayout mainLayout = new LinearLayout(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light));
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
                    button.setHeight((int)rowHeightPixels);
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
    public synchronized void onMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG, ttButton.getLabel() + " ACTION_DOWN");
                KeyDownParameters keyDownParameters = new KeyDownParameters(motionEvent.getEventTime(),motionEvent.getPressure(),motionEvent.getX(),motionEvent.getY());
                if(!ttButtonsDownParametersMap.isEmpty()) {
                    new FlightTimeCharacteristics(lastCommittedTTButton,
                            lastTTButtonDown,
                            getButtonDistanceMillimeters(lastCommittedTTButton,lastTTButtonDown),
                            0);
                    sendClickEvent(lastTTButtonDown);
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
                        Log.d(TAG,"flight time (millis): "+ lastCommittedTTButton.getLabel() + "->" + ttButton.getLabel()+": "+(correspondingKeyDownParameters.getTimeMillis()-lastTTButtonCommitTimeMillis) + " distance (mm): " + getButtonDistanceMillimeters(lastCommittedTTButton,ttButton));
                    }
                    sendClickEvent(ttButton);
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

    private synchronized void sendClickEvent(TTKeyboardButton ttButton) {
        clickListener.onKeyboardClick(ttButton,clickId);
        clickId++;
    }

    public String getUserId() {
        return userId;
    }
}
