package com.example.taptimingkeyboard.keyboard;

import android.view.MotionEvent;

/**
 * Used by {@link TapTimingKeyboard} logic. Stores information about button click (pressed or pressed+released).
 */
class TTButtonClickParameters {

    private final TTKeyboardButton ttKeyboardButton;
    private final long pressEventTimeMillis;
    private final float pressure;
    private final float x;
    private final float y;

    private boolean committed = false;
    private long clickId;
    private boolean released = false;
    private long releasedEventTime;
    private boolean flightTimeAborted=false;


    /**
     * Instantiates TTButtonClickParameters with parameters associated with button press (but not release).
     *
     * @param ttKeyboardButton     the tt keyboard button
     * @param pressEventTimeMillis the press event time millis
     * @param pressure             the pressure
     * @param x                    the x
     * @param y                    the y
     */
    public TTButtonClickParameters(TTKeyboardButton ttKeyboardButton, long pressEventTimeMillis, float pressure, float x, float y) {
        this.ttKeyboardButton = ttKeyboardButton;
        this.pressEventTimeMillis = pressEventTimeMillis;
        this.pressure = pressure;
        this.x = x;
        this.y = y;
    }

    /**
     * Gets ttKeyboardButton.
     *
     * @return The button which generated click.
     */
    public TTKeyboardButton getTtKeyboardButton() {
        return ttKeyboardButton;
    }

    /**
     * Gets pressEventTimeMillis
     *
     * @return The event time ({@link MotionEvent#getEventTime()}) of pressing the button in milliseconds.
     */
    public long getPressEventTimeMillis() {
        return pressEventTimeMillis;
    }

    /**
     * Gets pressure.
     *
     * @return The pressure of click ({@link MotionEvent#getPressure()} of {@link MotionEvent#ACTION_DOWN}).
     */
    public float getPressure() {
        return pressure;
    }

    /**
     * Gets x.
     *
     * @return The position of {@link MotionEvent#ACTION_DOWN} point in x dimension, in pixels, relative to the Button.
     */
    public float getX() {
        return x;
    }

    /**
     * Gets y.
     *
     * @return the The position of {@link MotionEvent#ACTION_DOWN} point in y dimension, in pixels, relative to the Button.
     */
    public float getY() {
        return y;
    }

    /**
     * Gets clickId.
     *
     * @return The unique click id.
     */
    public long getClickId() {
        return clickId;
    }

    /**
     * Gets releasedEventTime.
     *
     * @return The event time ({@link MotionEvent#getEventTime()}) of releasing the button in milliseconds.
     */
    public long getReleasedEventTime() {
        return releasedEventTime;
    }

    /**
     * Gets isCommitted.
     *
     * @return Whether the click is committed - have been sent by the {@link TapTimingKeyboard} to {@link TTKeyboardClickListener}
     */
    public boolean isCommitted() {
        return committed;
    }

    /**
     * Sets button in committed state.
     *
     * @param clickId An unique click id.
     */
    public void setCommitted(long clickId) {
        committed=true;
        this.clickId=clickId;
    }

    /**
     * Gets isReleased
     *
     * @return Whether the button have been released (the click is "complete").
     */
    public boolean isReleased() {
        return released;
    }

    /**
     * Sets released.
     *
     * @param releasedEventTime Mark this click as released ({@link #isReleased()})
     */
    public void setReleased(long releasedEventTime) {
        this.released = true;
        this.releasedEventTime=releasedEventTime;
    }

    /**
     * Get flightTimeAborted
     *
     * @return Whether {@link com.example.taptimingkeyboard.data.FlightTimeCharacteristics} where this click is the first click should be saved.
     */
    public boolean isFlightTimeAborted() {
        return flightTimeAborted;
    }

    /**
     * Abort flight time.
     * See {@link #isFlightTimeAborted()}
     */
    public void abortFlightTime() {
        flightTimeAborted=true;
    }
}
