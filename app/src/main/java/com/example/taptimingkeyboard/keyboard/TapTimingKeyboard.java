package com.example.taptimingkeyboard.keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.data.FlightTimeCharacteristics;
import com.example.taptimingkeyboard.data.KeyTapCharacteristics;
import com.example.taptimingkeyboard.data.TimingDataManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TapTimingKeyboard implements TTKeyboardMotionEventListener {

    public static final String TAG = TapTimingKeyboard.class.getName();

    public static final double MARGINS_PROPORTION = 0.05;
    public static final float BUTTON_TEXT_PROPORTION = 0.5f;

    private Context context;
    private AudioManager audioManager;

    private View tapTimingKeyboardView;
    private TTKeyboardLayout ttKeyboardLayout;
    private TTKeyboardClickListener clickListener;
    private Map<TTKeyboardButton, Button> buttonsMap = new HashMap<>();
    private String userId;
    private long sessionId;
    private boolean clickSound;
    private TimingDataManager timingDataManager;

    private double pixelSizeMmX;
    private double pixelSizeMmY;
    private float keyboardHeightPixels;

    //which buttons are currently pressed (but not released) and associated MotionEvents
    private Map<TTKeyboardButton,KeyDownParameters> ttButtonsDownParametersMap = Collections.synchronizedMap(new HashMap<TTKeyboardButton, KeyDownParameters>());
    private TTKeyboardButton lastTTButtonDown = null;
    private TTButtonClick lastTTButtonClick = null;
    private WaitingFlightTimeCharacteristics waitingFlightTimeCharacteristics = null;

    private long clickId = 0;

    public TapTimingKeyboard(Context context, TTKeyboardLayout.Layout layout, TTKeyboardClickListener clickListener) {
        this.context=context;
        this.clickListener=clickListener;
        timingDataManager=new TimingDataManager(context);
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        userId=sharedPreferences.getString("user_id","");
        clickSound=sharedPreferences.getBoolean("click_sound",true);
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        float screenHeightPixels=point.y;
        display.getRealSize(point);
        Log.i(TAG,"screen size (px): "+point.x+"x"+point.y);
        double longerScreenDimension = Double.parseDouble(sharedPreferences.getString("longer_screen_dimension",""));
        double shorterScreenDimension = Double.parseDouble(sharedPreferences.getString("shorter_screen_dimension",""));
        if(longerScreenDimension==0 || shorterScreenDimension==0)
            Toast.makeText(context,"warning: no screen size provided in settings",Toast.LENGTH_SHORT).show();
        else {
            pixelSizeMmX = (point.x > point.y) ? (longerScreenDimension / point.x) : (shorterScreenDimension / point.x);
            pixelSizeMmY = (point.y > point.x) ? (longerScreenDimension / point.y) : (shorterScreenDimension / point.y);
            Log.i(TAG, "pixel size x (mm) = " + pixelSizeMmX + " pixel size  (mm) = " + pixelSizeMmY);
        }
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            keyboardHeightPixels=sharedPreferences.getInt("height_landscape",0)*0.01f*screenHeightPixels;
        else
            keyboardHeightPixels=sharedPreferences.getInt("height_portrait",0)*0.01f*screenHeightPixels;
        ttKeyboardLayout=TTKeyboardLayout.withLayout(layout);
        tapTimingKeyboardView = createView(context);
    }

    public void startTestSession(long sessionId) {
        this.sessionId=sessionId;
        timingDataManager.startTestSession();
    }

    public void endTestSession() {
        timingDataManager.endTestSession();
    }

    public void acceptButtonClick(long clickId) {
        timingDataManager.acceptButtonClick(clickId);
    }

    public void rejectButtonClick(long clickId) {
        timingDataManager.rejectButtonClick(clickId);
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
        TextView buttonFrom = buttonsMap.get(from);
        TextView buttonTo = buttonsMap.get(to);
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

    public double getButtonSizeX(TTKeyboardButton ttButton) {
        return buttonsMap.get(ttButton).getWidth();
    }

    public double getButtonSizeY(TTKeyboardButton ttButton) {
        return buttonsMap.get(ttButton).getHeight();
    }

    private View createView(Context context) {
        float rowHeightPixels = keyboardHeightPixels/ttKeyboardLayout.getRows().size();
        LinearLayout mainLayout = new LinearLayout(new ContextThemeWrapper(context, R.style.AppTheme));
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mainLayout.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        for(Iterator<TTKeyboardRow> itRow = ttKeyboardLayout.getRows().iterator(); itRow.hasNext();) {
            TTKeyboardRow row = itRow.next();
            LinearLayout rowLinearLayout = new LinearLayout(context);
            rowLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for(Iterator<TTKeyboardElement> itElement = row.getElements().iterator(); itElement.hasNext();) {
                TTKeyboardElement element = itElement.next();
                if(element instanceof TTKeyboardButton) {
                    final TTKeyboardButton ttButton = (TTKeyboardButton)element;
                    Button button = new Button(new ContextThemeWrapper(context,R.style.ttButton),null,0);
                    button.setText(ttButton.getLabel());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,ttButton.getSize());
                    button.setLayoutParams(layoutParams);
                    button.setHeight((int)rowHeightPixels);
                    //TODO should be restricted also by button width
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX,rowHeightPixels*BUTTON_TEXT_PROPORTION);
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
        long currentTimestampMillis = System.currentTimeMillis();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG, ttButton.getLabel() + " ACTION_DOWN");
                KeyDownParameters keyDownParameters = new KeyDownParameters(motionEvent.getEventTime(),motionEvent.getPressure(),motionEvent.getX(),motionEvent.getY());
                if(!ttButtonsDownParametersMap.isEmpty() && lastTTButtonClick!=null && lastTTButtonClick.getTtButton()!=lastTTButtonDown) {
                    Log.d(TAG,"zero flight time: "+ lastTTButtonDown.getLabel() + "->" + ttButton.getLabel());
                    FlightTimeCharacteristics flightTimeCharacteristics = new FlightTimeCharacteristics(
                            currentTimestampMillis,
                            (char)lastTTButtonDown.getCode(),
                            (char)ttButton.getCode(),
                            getButtonDistanceMillimeters(ttButton,lastTTButtonDown),
                            0,
                            userId,
                            sessionId);
                    if(waitingFlightTimeCharacteristics !=null)
                        timingDataManager.addFlightTimeCharacteristics(waitingFlightTimeCharacteristics.getFlightTimeCharacteristics(), waitingFlightTimeCharacteristics.getFirstClickId(),clickId);
                    waitingFlightTimeCharacteristics =new WaitingFlightTimeCharacteristics(flightTimeCharacteristics,clickId);
                    ttButtonsDownParametersMap.get(lastTTButtonDown).setCommitted(clickId);
                    lastTTButtonClick=new TTButtonClick(lastTTButtonDown,clickId,motionEvent.getEventTime());
                    sendClickEvent(lastTTButtonDown);
                }
                ttButtonsDownParametersMap.put(ttButton,keyDownParameters);
                lastTTButtonDown=ttButton;
                break;
            case MotionEvent.ACTION_UP:
                Log.v(TAG, ttButton.getLabel() + " ACTION_UP");
                if(!ttButtonsDownParametersMap.containsKey(ttButton))
                    return;
                KeyDownParameters correspondingKeyDownParameters = ttButtonsDownParametersMap.get(ttButton);
                ttButtonsDownParametersMap.remove(ttButton);
                long holdTimeMillis = motionEvent.getEventTime() - correspondingKeyDownParameters.getTimeMillis();
                double imprecisionX=2*(correspondingKeyDownParameters.getX()/getButtonSizeX(ttButton))-1;
                double imprecisionY=2*(correspondingKeyDownParameters.getY()/getButtonSizeY(ttButton))-1;
                Log.d(TAG,"tapped button: " + ttButton.getLabel() + " hold time (millis): " + holdTimeMillis + " pressure: " + correspondingKeyDownParameters.getPressure()+ " imprecision (x, y): "+imprecisionX+","+imprecisionY);
                KeyTapCharacteristics keyTapCharacteristics = new KeyTapCharacteristics(
                        currentTimestampMillis,
                        (char)ttButton.getCode(),
                        holdTimeMillis,
                        correspondingKeyDownParameters.getPressure(),
                        (float)imprecisionX,
                        (float)imprecisionY,
                        userId,
                        sessionId);
                timingDataManager.addKeyTapCharacteristics(keyTapCharacteristics,correspondingKeyDownParameters.isCommitted()?correspondingKeyDownParameters.getClickId():clickId);
                if(!correspondingKeyDownParameters.isCommitted()) {
                    if(lastTTButtonClick != null) {
                        if(waitingFlightTimeCharacteristics !=null) { //last click was by pressing a new key without releasing previous
                            timingDataManager.addFlightTimeCharacteristics(waitingFlightTimeCharacteristics.getFlightTimeCharacteristics(), waitingFlightTimeCharacteristics.getFirstClickId(), clickId);
                            waitingFlightTimeCharacteristics = null;
                        } else { //last click was by pressing and then releasing a key
                            Log.d(TAG,"flight time (millis): "+ lastTTButtonClick.getTtButton().getLabel() + "->" + ttButton.getLabel()+": "+(correspondingKeyDownParameters.getTimeMillis()-lastTTButtonClick.getClickTimestampMillis()) + " distance (mm): " + getButtonDistanceMillimeters(lastTTButtonClick.getTtButton(),ttButton));
                            FlightTimeCharacteristics flightTimeCharacteristics=new FlightTimeCharacteristics(
                                    currentTimestampMillis,
                                    (char)lastTTButtonClick.getTtButton().getCode(),
                                    (char)ttButton.getCode(),
                                    getButtonDistanceMillimeters(lastTTButtonClick.getTtButton(),ttButton),
                                    correspondingKeyDownParameters.getTimeMillis()-lastTTButtonClick.getClickTimestampMillis(),
                                    userId,
                                    sessionId);
                            timingDataManager.addFlightTimeCharacteristics(flightTimeCharacteristics, lastTTButtonClick.getClickId(), clickId);
                        }
                    }
                    lastTTButtonClick = new TTButtonClick(ttButton,clickId,motionEvent.getEventTime());
                    sendClickEvent(ttButton);
                }
                break;
        }
    }

    public void abortCurrentFlightTime() {
        lastTTButtonClick=null;
    }

    private long sendClickEvent(TTKeyboardButton ttButton) {
        if(clickSound) {
            if (audioManager == null)
                audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            //TODO this plays only when system wide clicks sounds are enabled
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
        clickListener.onKeyboardClick(ttButton,clickId);
        return clickId++;
    }

    public String getUserId() {
        return userId;
    }
}
