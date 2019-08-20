package com.example.taptimingkeyboard.keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
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

import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.data.FlightTimeCharacteristics;
import com.example.taptimingkeyboard.data.KeyTapCharacteristics;
import com.example.taptimingkeyboard.data.RemotePreferences;
import com.example.taptimingkeyboard.data.TimingDataManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TapTimingKeyboard implements TTKeyboardMotionEventListener {

    public static final String TAG = TapTimingKeyboard.class.getName();

    public static final float BUTTON_TEXT_PROPORTION = 0.4f;
    public static final float INCH_TO_MM = 25.4f;

    private Context context;
    private AudioManager audioManager;
    private Vibrator vibrator;

    private View tapTimingKeyboardView;
    private TTKeyboardLayout ttKeyboardLayout;
    private TTKeyboardClickListener clickListener;
    private Map<TTKeyboardButton, Button> buttonsMap = new HashMap<>();
    private long userId;
    private long sessionId;
    private boolean clickSound;
    private float clickVol;
    private boolean vibrations;
    private int vibrationDuration;
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

    public TapTimingKeyboard(Context context, TTKeyboardLayout.Layout layout, TTKeyboardClickListener clickListener, @Nullable RemotePreferences remotePreferences, @Nullable Long userId) {
        this.context=context;
        this.clickListener=clickListener;
        timingDataManager=new TimingDataManager(context);
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.userId=(userId!=null)?userId:Long.parseLong(sharedPreferences.getString("user_id","0"));
        clickSound=(remotePreferences!=null&&remotePreferences.getSound()!=null)?remotePreferences.getSound():sharedPreferences.getBoolean("click_sound",true);
        clickVol=(remotePreferences!=null&&remotePreferences.getVolume()!=null)?remotePreferences.getVolume()/100.f:sharedPreferences.getInt("click_volume",0)/100.f;
        vibrations=(remotePreferences!=null&&remotePreferences.getVibrations()!=null)?remotePreferences.getVibrations():sharedPreferences.getBoolean("vibrations",false);
        vibrationDuration=(remotePreferences!=null&&remotePreferences.getVibrationDuration()!=null)?remotePreferences.getVibrationDuration():sharedPreferences.getInt("vibration_duration",0);
        int heightPortrait = (remotePreferences!=null&&remotePreferences.getSizePortrait()!=null)?remotePreferences.getSizePortrait():sharedPreferences.getInt("height_portrait",0);
        int heightLandscape = (remotePreferences!=null&&remotePreferences.getSizeLandscape()!=null)?remotePreferences.getSizeLandscape():sharedPreferences.getInt("height_landscape",0);
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        float screenHeightPixels=point.y;
        display.getRealSize(point);
        Log.i(TAG,"screen size (px): "+point.x+"x"+point.y);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        Log.i(TAG,"screen dpi, x: "+displayMetrics.xdpi+" y: "+displayMetrics.ydpi);
        pixelSizeMmX=1/(displayMetrics.xdpi/INCH_TO_MM);
        pixelSizeMmY=1/(displayMetrics.ydpi/INCH_TO_MM);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            keyboardHeightPixels=heightLandscape*0.01f*screenHeightPixels;
        else
            keyboardHeightPixels=heightPortrait*0.01f*screenHeightPixels;
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
        Point distancesPx = getButtonDistancePx(from,to);
        double distanceXMm=distancesPx.x*pixelSizeMmX;
        double distanceYMm=distancesPx.y*pixelSizeMmY;
        return Math.sqrt(Math.pow(distanceXMm,2)+Math.pow(distanceYMm,2));
    }

    public Point getButtonDistancePx(TTKeyboardButton from, TTKeyboardButton to) {
        if(buttonsMap.isEmpty())
            throw new IllegalStateException("called getButtonDistance before creating layout view");
        TextView buttonFrom = buttonsMap.get(from);
        TextView buttonTo = buttonsMap.get(to);
        Rect buttonFromRect = new Rect();
        Rect buttonToRect = new Rect();
        buttonFrom.getDrawingRect(buttonFromRect);
        ((LinearLayout)tapTimingKeyboardView).offsetDescendantRectToMyCoords(buttonFrom,buttonFromRect);
        int xFrom=buttonFromRect.left;
        int yFrom=buttonFromRect.top;
        buttonTo.getDrawingRect(buttonToRect);
        ((LinearLayout)tapTimingKeyboardView).offsetDescendantRectToMyCoords(buttonTo,buttonToRect);
        int xTo=buttonToRect.left;
        int yTo=buttonToRect.top;
        return new Point(Math.abs(xFrom-xTo),Math.abs(yFrom-yTo));
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
                playClick();
                vibrate();
                Log.v(TAG, ttButton.getLabel() + " ACTION_DOWN");
                KeyDownParameters keyDownParameters = new KeyDownParameters(motionEvent.getEventTime(),motionEvent.getPressure(),motionEvent.getX(),motionEvent.getY());
                if(!ttButtonsDownParametersMap.isEmpty() && lastTTButtonClick!=null && lastTTButtonClick.getTtButton()!=lastTTButtonDown) {
                    Log.d(TAG,"zero flight time: "+ lastTTButtonDown.getLabel() + "->" + ttButton.getLabel());
                    FlightTimeCharacteristics flightTimeCharacteristics = new FlightTimeCharacteristics(
                            currentTimestampMillis,
                            (char)lastTTButtonDown.getCode(),
                            (char)ttButton.getCode(),
                            getButtonDistancePx(ttButton,lastTTButtonDown).x,
                            getButtonDistancePx(ttButton,lastTTButtonDown).y,
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
                                    getButtonDistancePx(lastTTButtonClick.getTtButton(),ttButton).x,
                                    getButtonDistancePx(lastTTButtonClick.getTtButton(),ttButton).y,
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
        clickListener.onKeyboardClick(ttButton,clickId);
        return clickId++;
    }

    private void playClick() {
        if(clickSound) {
            if (audioManager == null)
                audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD,clickVol);
        }
    }

    private void vibrate() {
        if(vibrations) {
            if(vibrator==null) {
                vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration,VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(vibrationDuration);
            }
        }
    }

    public long getUserId() {
        return userId;
    }
}
