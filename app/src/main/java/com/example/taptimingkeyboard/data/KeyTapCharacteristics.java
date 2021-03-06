package com.example.taptimingkeyboard.data;

import android.view.MotionEvent;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * KeyTapCharacteristics stores information about a keyboard button click such as click hold time, pressure, precision
 */
@Entity(tableName = "key_tap_characteristics")
public class KeyTapCharacteristics {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long timestamp;
    private int keyCharacter;
    private long holdTimeMillis;
    private float pressure;
    private float imprecisionX;
    private float imprecisionY;
    private long userId;
    private long sessionId;

    /**
     * Instantiates a new Key tap characteristics.
     *
     * @param timestamp      see {@link #getTimestamp()}
     * @param keyCharacter   see {@link #getKeyCharacter()}
     * @param holdTimeMillis see {@link #getHoldTimeMillis()}
     * @param pressure       see {@link #getPressure()}
     * @param imprecisionX   see {@link #getImprecisionX()}
     * @param imprecisionY   see {@link #getImprecisionY()}
     * @param userId         see {@link #getUserId()}
     * @param sessionId      see {@link #getSessionId()}
     */
    public KeyTapCharacteristics(long timestamp, int keyCharacter, long holdTimeMillis, float pressure, float imprecisionX, float imprecisionY, long userId, long sessionId) {
        this.timestamp = timestamp;
        this.keyCharacter = keyCharacter;
        this.holdTimeMillis = holdTimeMillis;
        this.pressure = pressure;
        this.imprecisionX = imprecisionX;
        this.imprecisionY = imprecisionY;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    /**
     * Sets id.
     *
     * @param id see {@link #getId()}
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets id.
     *
     * @return The unique id (auto generated by Room Library)
     */
    public long getId() {
        return id;
    }

    /**
     * Gets timestamp.
     *
     * @return The timestamp of releasing the button in milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets keyCharacter.
     *
     * @return The character of clicked button.
     */
    public int getKeyCharacter() {
        return keyCharacter;
    }

    /**
     * Gets holdTimeMillis.
     *
     * @return The time between pressing and releasing the button in milliseconds.
     */
    public long getHoldTimeMillis() {
        return holdTimeMillis;
    }

    /**
     * Gets pressure.
     *
     * @return The pressure of keyboard Button's KEY_DOWN event determined by Android's {@link MotionEvent#getPressure()}
     */
    public float getPressure() {
        return pressure;
    }

    /**
     * Gets imprecisionX.
     *
     * @return The distance in x dimension between the click point (keyboard Button's KEY_DOWN event) and center of the Button.
     * The value is between -1 and 1 where -1 for the left edge of the button and 1 for the right edge of the button, scaled linearly.
     */
    public float getImprecisionX() {
        return imprecisionX;
    }

    /**
     * Gets imprecisionY.
     *
     * @return The distance in y dimension between the click point (keyboard Button's KEY_DOWN event) and center of the Button.
     * The value is between -1 and 1 where -1 for the top edge of the button and 1 for the bottom edge of the button, scaled linearly.
     */
    public float getImprecisionY() {
        return imprecisionY;
    }

    /**
     * Gets userId.
     *
     * @return The id of the user. It's determined in keyboard settings or test session activity, depending on keyboard mode.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Gets sessionId.
     *
     * @return The id of test session. It's determined in test session activity. Every started session has unique (incremental) id.
     */
    public long getSessionId() {
        return sessionId;
    }
}
