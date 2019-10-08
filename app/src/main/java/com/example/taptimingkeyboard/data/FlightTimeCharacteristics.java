package com.example.taptimingkeyboard.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * FlightTimeCharacteristics stores information about two subsequent keyboard button clicks
 * The essential parameter is "flight time" see {@link #getFlightTimeMillis()}
 */
@Entity(tableName = "flight_time_characteristics")
public class FlightTimeCharacteristics {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long timestamp;
    private int charFrom;
    private int charTo;
    //this is calculated with: new Point(xFrom-xTo,yFrom-yTo), From - first button coordinates, To - seconds button coordinates
    private double distancePixelsX;
    private double distancePixelsY;
    private double distanceMm;
    private long flightTimeMillis;
    private long userId;
    private long sessionId;

    /**
     * Instantiates a new Flight time characteristics.
     *
     * @param timestamp       see {@link #getTimestamp()}
     * @param charFrom        see {@link #getCharFrom()}
     * @param charTo          see {@link #getCharTo()}
     * @param distancePixelsX see {@link #getDistancePixelsX()}
     * @param distancePixelsY see {@link #getDistancePixelsY()}
     * @param distanceMm      see {@link #getDistanceMm()}
     * @param flightTimeMillis      see {@link #getFlightTimeMillis()}
     * @param userId          see {@link #getUserId()}
     * @param sessionId       see {@link #getSessionId()}
     */
    public FlightTimeCharacteristics(long timestamp, int charFrom, int charTo, double distancePixelsX, double distancePixelsY, double distanceMm, long flightTimeMillis, long userId, long sessionId) {
        this.timestamp = timestamp;
        this.charFrom = charFrom;
        this.charTo = charTo;
        this.distancePixelsX = distancePixelsX;
        this.distancePixelsY = distancePixelsY;
        this.distanceMm = distanceMm;
        this.flightTimeMillis = flightTimeMillis;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    /**
     * Sets id.
     *
     * @param id See {@link #getId()}
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
     * @return The timestamp of the event in milliseconds. Specifically the time of pressing down the second (latter) button.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets charFrom.
     *
     * @return The character of the the button pressed (tapped down) earlier
     */
    public int getCharFrom() {
        return charFrom;
    }

    /**
     * Gets charTo.
     *
     * @return The character of the the button pressed (tapped down) later
     */
    public int getCharTo() {
        return charTo;
    }

    /**
     * Gets distancePixelsX.
     *
     * @return The distance between buttons in x dimension
     */
    public double getDistancePixelsX() {
        return distancePixelsX;
    }

    /**
     * Gets distancePixelsY.
     *
     * @return The distance between buttons in y dimension
     */
    public double getDistancePixelsY() {
        return distancePixelsY;
    }

    /**
     * Gets distanceMm.
     *
     * @return The distance between buttons in millimeters based on distance in pixels and Android's {@link android.util.DisplayMetrics}
     */
    public double getDistanceMm() {
        return distanceMm;
    }

    /**
     * Gets flightTimeMillis
     *
     * @return The time in milliseconds between releasing the first key and pressing the second key.
     * This could be be less than zero in following scenario: pressing first key, pressing second key, releasing second key, releasing first key
     */
    public long getFlightTimeMillis() {
        return flightTimeMillis;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash=31*hash+(int)id;
        hash=31*hash+(int)timestamp;
        return hash;
    }
}
