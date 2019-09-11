package com.example.taptimingkeyboard.keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
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
import androidx.preference.PreferenceManager;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.activity.UiSounds;
import com.example.taptimingkeyboard.data.FlightTimeCharacteristics;
import com.example.taptimingkeyboard.data.KeyTapCharacteristics;
import com.example.taptimingkeyboard.data.RemotePreferences;
import com.example.taptimingkeyboard.data.TimingDataManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

/**
 * Main TapTimingKeyboard class
 * This class creates keyboard View based on {@link TTKeyboardLayout} , maintains touch events and sends click events to {@link TTKeyboardClickListener}.
 * The most important functionality of TapTimingKeyboard is saving data about user interaction with keys such as times between subsequent clicks, times of button hold, pressure of clicks, precision of clicks ({@link FlightTimeCharacteristics}, {@link KeyTapCharacteristics}).
 */
public class TapTimingKeyboard {

    private static final String TAG = TapTimingKeyboard.class.getName();
    private static final float BUTTON_TEXT_PROPORTION = 0.4f;
    private static final float INCH_TO_MM = 25.4f;

    private final Context context;
    private final UiSounds uiSounds;
    private final TimingDataManager timingDataManager;

    private final View tapTimingKeyboardView;
    private final TTKeyboardLayout ttKeyboardLayout;
    private final TTKeyboardClickListener clickListener;
    private final Map<TTKeyboardButton, Button> buttonsMap = new HashMap<>();

    private double pixelSizeMmX;
    private double pixelSizeMmY;
    private float keyboardHeightPixels;

    private long userId;
    private long sessionId;
    private boolean clickSound;
    private float clickVol;
    private boolean vibrations;
    private int vibrationDuration;
    private int heightPortrait;
    private int heightLandscape;

    private final LinkedList<TTButtonClickParameters> buttonClickParametersList = new LinkedList<>();
    private long clickId = 0;

    /**
     * {@link TapTimingKeyboard} constructor
     *
     * Initializes keyboard View and keyboard logic
     *
     * @param context activity or service (when used as {@link android.view.inputmethod.InputMethod}) context
     * @param layout defines keyboard buttons layout
     * @param clickListener gets notified about events recognized as keyboard button clicks
     * @param remotePreferences keyboard uses preferences set in application's {@link SharedPreferences} but some can be overridden by passing this object
     * @param userId TapTimingKeyboard writes user interaction data to the database userId is taken from application SharedPreferences but it can be overridden by this parameter
     */
    public TapTimingKeyboard(Context context, TTKeyboardLayout.Layout layout, TTKeyboardClickListener clickListener, @Nullable RemotePreferences remotePreferences, @Nullable Long userId) {
        this.context=context;
        this.clickListener=clickListener;
        timingDataManager=new TimingDataManager(context);
        loadPreferences(remotePreferences,userId);
        loadDisplayRelatedParameters();
        uiSounds=new UiSounds(context);
        if(clickSound)
            uiSounds.initClickSound();
        if(vibrations)
            uiSounds.initVibrator();
        ttKeyboardLayout=TTKeyboardLayout.withLayout(layout);
        tapTimingKeyboardView=createView();
    }

    /**
     * Loads preferences from application default SharedPreferences
     * @param remotePreferences values passed in this object override values from SharedPreferences
     * @param userId override user id from SharedPreferences
     */
    private void loadPreferences(@Nullable RemotePreferences remotePreferences, @Nullable Long userId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.userId=(userId!=null)?userId:sharedPreferences.getLong("user_id",1);
        clickSound=(remotePreferences!=null&&remotePreferences.getSound()!=null)?remotePreferences.getSound():sharedPreferences.getBoolean("click_sound",true);
        clickVol=(remotePreferences!=null&&remotePreferences.getVolume()!=null)?remotePreferences.getVolume()/100.f:sharedPreferences.getInt("click_volume",0)/100.f;
        vibrations=(remotePreferences!=null&&remotePreferences.getVibrations()!=null)?remotePreferences.getVibrations():sharedPreferences.getBoolean("vibrations",false);
        vibrationDuration=(remotePreferences!=null&&remotePreferences.getVibrationDuration()!=null)?remotePreferences.getVibrationDuration():sharedPreferences.getInt("vibration_duration",0);
        heightPortrait=(remotePreferences!=null&&remotePreferences.getSizePortrait()!=null)?remotePreferences.getSizePortrait():sharedPreferences.getInt("height_portrait",0);
        heightLandscape=(remotePreferences!=null&&remotePreferences.getSizeLandscape()!=null)?remotePreferences.getSizeLandscape():sharedPreferences.getInt("height_landscape",0);
    }

    /**
     * Uses android api to get screen parameters: size and DPI and. Sets keyboardHeightPixels using screen parameters.
     */
    private void loadDisplayRelatedParameters() {
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
    }

    /**
     * Creates keyboard {@link View} which is vertical {@link LinearLayout} containing keyboard buttons ({@link Button}) in rows (row is a horizontal {@link LinearLayout})
     * {@link MotionEvent} from every button calls {@link #onMotionEvent(TTKeyboardButton, MotionEvent)}
     *
     * @return returns created {@link View}
     */
    private View createView() {
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

    /**
     * Start test session mode.
     * In normal mode user interaction parameters for every click are saved in database.
     * In test session mode, click should be accepted by calling {@link #acceptButtonClick(long)} or rejected by calling {@link #rejectButtonClick(long)}.
     * Only parameters associated with accepted clicks are saved into database.
     * @param sessionId in session mode interaction data is saved with sessionId which should be generated by application maintaining test session (e.g. {@link com.example.taptimingkeyboard.activity.TestSessionActivity})
     */
    public void startTestSession(long sessionId) {
        this.sessionId=sessionId;
        timingDataManager.startTestSession();
    }

    /**
     * Ends test session mode {@link #startTestSession(long)}
     * TapTimingKeyboard returns to the "normal" mode
     */
    public void endTestSession() {
        timingDataManager.endTestSession();
    }

    /**
     * Accepts button click (when test application recognized specific click as correct according to test session specifications)
     * This should be used in test session mode {@link #startTestSession(long)}
     * @param clickId click id received in {@link TTKeyboardClickListener#onKeyboardClick(TTKeyboardButton, long)}
     */
    public void acceptButtonClick(long clickId) {
        timingDataManager.acceptButtonClick(clickId);
    }

    /**
     * Rejects button click (when test application recognized specific click as incorrect according to test session specifications)
     * This should be used in test session mode {@link #startTestSession(long)}
     * @param clickId click id received in {@link TTKeyboardClickListener#onKeyboardClick(TTKeyboardButton, long)}
     */
    public void rejectButtonClick(long clickId) {
        timingDataManager.rejectButtonClick(clickId);
    }

    /**
     * @return keyboard View which contains TapTimingKeyboard layout (buttons ordered according to {@link TTKeyboardLayout})
     */
    public View getView() {
        return tapTimingKeyboardView;
    }

    /**
     * Get distance between two buttons in current keyboard layout
     * @param from first button
     * @param to seconds button
     * @return distance in mm (calculated using data from Android's {@link DisplayMetrics}) between top-left edges of specified buttons
     */
    private double getButtonDistanceMillimeters(TTKeyboardButton from, TTKeyboardButton to) {
        Point distancesPx = getButtonDistancePx(from,to);
        double distanceXMm=distancesPx.x*pixelSizeMmX;
        double distanceYMm=distancesPx.y*pixelSizeMmY;
        return Math.sqrt(Math.pow(distanceXMm,2)+Math.pow(distanceYMm,2));
    }

    /**
     * Get distance between two buttons in current keyboard layout
     * @param from first button
     * @param to seconds button
     * @return distance in pixels between top-left edges of specified buttons
     */
    private Point getButtonDistancePx(TTKeyboardButton from, TTKeyboardButton to) {
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

    /**
     * @param ttButton
     * @return returns button's size in X dimension
     */
    private double getButtonSizeX(TTKeyboardButton ttButton) {
        return buttonsMap.get(ttButton).getWidth();
    }

    /**
     * @param ttButton
     * @return returns button's size in Y dimension
     */
    private double getButtonSizeY(TTKeyboardButton ttButton) {
        return buttonsMap.get(ttButton).getHeight();
    }

    /**
     * Handles motion events ({@link MotionEvent}) sent by keyboard's buttons.
     * If button click is recognized, notification is sent to {@link TTKeyboardClickListener}, with unique clickId.
     * User interaction parameters ({@link KeyTapCharacteristics}, {@link FlightTimeCharacteristics}) are sent to {@link TimingDataManager}
     * @param ttButton button for which {@link MotionEvent} happened
     * @param motionEvent motion event specification
     */
    public synchronized void onMotionEvent(TTKeyboardButton ttButton, MotionEvent motionEvent) {
        long currentTimestampMillis = System.currentTimeMillis();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG, ttButton.getLabel() + " ACTION_DOWN");
                playClick();
                vibrateClick();
                if(!buttonClickParametersList.isEmpty()&&!buttonClickParametersList.getLast().isCommitted()) {
                    buttonClickParametersList.getLast().setCommitted(sendClickEvent(buttonClickParametersList.getLast().getTtKeyboardButton()));
                }
                buttonClickParametersList.add(new TTButtonClickParameters(ttButton,motionEvent.getEventTime(),motionEvent.getPressure(),motionEvent.getX(),motionEvent.getY()));
                break;
            case MotionEvent.ACTION_UP:
                Log.v(TAG, ttButton.getLabel() + " ACTION_UP");
                TTButtonClickParameters currentButtonClick =null;
                TTButtonClickParameters previousButtonClick=null;
                TTButtonClickParameters nextButtonClick=null;
                int firstItemsToRemove=0;
                ListIterator<TTButtonClickParameters> buttonClickIterator = buttonClickParametersList.listIterator();
                while(buttonClickIterator.hasNext()) {
                    TTButtonClickParameters currentIteratorClick = buttonClickIterator.next();
                    if(previousButtonClick==buttonClickParametersList.peekFirst()&&previousButtonClick.isReleased()&&currentIteratorClick.isReleased())
                        firstItemsToRemove++;
                    if(currentIteratorClick.getTtKeyboardButton()==ttButton&&!currentIteratorClick.isReleased()) {
                        currentButtonClick=currentIteratorClick;
                        if(buttonClickIterator.hasNext())
                            nextButtonClick=buttonClickIterator.next();
                        break;
                    }
                    previousButtonClick=currentIteratorClick;
                }
                for (int i = 0; i < firstItemsToRemove; i++)
                    buttonClickParametersList.removeFirst();
                if(currentButtonClick ==null)
                    return;
                currentButtonClick.setReleased(motionEvent.getEventTime());
                if(!currentButtonClick.isCommitted()) {
                    currentButtonClick.setCommitted(sendClickEvent(ttButton));
                }
                saveKeyTapCharacteristics(currentButtonClick,currentTimestampMillis);
                if(previousButtonClick!=null) {
                    if(previousButtonClick.isReleased()) { //two subsequent keys released, flight time characteristics could be saved
                        saveFlightTimeCharacteristics(previousButtonClick, currentButtonClick,currentTimestampMillis);
                    }
                }
                if(nextButtonClick!=null) {
                    if(nextButtonClick.isReleased()) { //two subsequent keys released, flight time characteristics could be saved
                        saveFlightTimeCharacteristics(currentButtonClick,nextButtonClick,currentTimestampMillis);
                    }
                }
                break;
        }
    }

    /**
     * Creates {@link FlightTimeCharacteristics} and sends it to {@link TimingDataManager} to be saved in Room database or rejected
     * @param firstButtonClickParameters specifications of the first button click (press+release)
     * @param secondButtonClickParameters specifications of the second button click (press+release)
     * @param currentTimestampMillis timestamp of second button release in milliseconds
     */
    private void saveFlightTimeCharacteristics(TTButtonClickParameters firstButtonClickParameters, TTButtonClickParameters secondButtonClickParameters, long currentTimestampMillis) {
        if(firstButtonClickParameters.isFlightTimeAborted())
            return;
        Log.d(TAG,String.format("FlightTimeCharacteristics '%s'->'%s', time: %d",
                (char)firstButtonClickParameters.getTtKeyboardButton().getCode(),
                (char)secondButtonClickParameters.getTtKeyboardButton().getCode(),
                secondButtonClickParameters.getPressEventTimeMillis()- firstButtonClickParameters.getReleasedEventTime()));
        long secondHoldTime = secondButtonClickParameters.getReleasedEventTime()- secondButtonClickParameters.getPressEventTimeMillis();
        timingDataManager.addFlightTimeCharacteristics(new FlightTimeCharacteristics(
                currentTimestampMillis-secondHoldTime,
                firstButtonClickParameters.getTtKeyboardButton().getCode(),
                secondButtonClickParameters.getTtKeyboardButton().getCode(),
                getButtonDistancePx(firstButtonClickParameters.getTtKeyboardButton(), secondButtonClickParameters.getTtKeyboardButton()).x,
                getButtonDistancePx(firstButtonClickParameters.getTtKeyboardButton(), secondButtonClickParameters.getTtKeyboardButton()).y,
                getButtonDistanceMillimeters(firstButtonClickParameters.getTtKeyboardButton(), secondButtonClickParameters.getTtKeyboardButton()),
                secondButtonClickParameters.getPressEventTimeMillis()- firstButtonClickParameters.getReleasedEventTime(),
                userId,
                sessionId
        ), firstButtonClickParameters.getClickId(), secondButtonClickParameters.getClickId());
    }


    /**
     * Creates {@link KeyTapCharacteristics} and sends it to {@link TimingDataManager} to be saved in Room database or rejected
     * @param buttonClickParameters specifications of the button click (press+release)
     * @param currentTimestampMillis timestamp of button release in milliseconds
     */
    private void saveKeyTapCharacteristics(TTButtonClickParameters buttonClickParameters, long currentTimestampMillis) {
        Log.d(TAG,String.format("KeyTapCharacteristics '%s', hold time: %d",
                (char) buttonClickParameters.getTtKeyboardButton().getCode(),
                buttonClickParameters.getReleasedEventTime()- buttonClickParameters.getPressEventTimeMillis()));
        float imprecisionX=(float)(2*(buttonClickParameters.getX()/getButtonSizeX(buttonClickParameters.getTtKeyboardButton()))-1);
        float imprecisionY=(float)(2*(buttonClickParameters.getY()/getButtonSizeY(buttonClickParameters.getTtKeyboardButton()))-1);
        timingDataManager.addKeyTapCharacteristics(new KeyTapCharacteristics(
                currentTimestampMillis,
                buttonClickParameters.getTtKeyboardButton().getCode(),
                buttonClickParameters.getReleasedEventTime()- buttonClickParameters.getPressEventTimeMillis(),
                buttonClickParameters.getPressure(),
                imprecisionX,
                imprecisionY,
                userId,
                sessionId
        ), buttonClickParameters.getClickId());
    }

    /**
     * Don't save {@link FlightTimeCharacteristics} between last and future click
     * Use case: when hiding keyboard. Flight time for two clicks between which keyboard has been hidden and showed again probably isn't usable information.
     */
    public void abortCurrentFlightTime() {
        buttonClickParametersList.peekLast().abortFlightTime();
    }

    /**
     * Sends keyboard button click event with unique click id to {@link #clickListener}
     * @param ttButton
     * @return click id which has been sent
     */
    private long sendClickEvent(TTKeyboardButton ttButton) {
        Log.d(TAG,String.format("click event for '%s', id: %d",(char)ttButton.getCode(),clickId));
        clickListener.onKeyboardClick(ttButton,clickId);
        return clickId++;
    }

    /**
     * Play sound on click if enabled
     */
    private void playClick() {
        if(clickSound) {
            uiSounds.playClickSound(clickVol);
        }
    }

    /**
     * Vibrate on click if enabled
     */
    private void vibrateClick() {
        if(vibrations) {
            uiSounds.vibrateMs(vibrationDuration);
        }
    }

}
