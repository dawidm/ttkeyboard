package com.example.taptimingkeyboard.data;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Stores application preferences that could be downloaded from server and override values set in keyboard preferences (on the device).
 */
public class RemotePreferences {

    public static final int ORIENTATION_PORTRAIT=0;
    public static final int ORIENTATION_LANDSCAPE=1;

    @Nullable
    private Integer sizePortrait;
    @Nullable
    private Integer sizeLandscape;
    @Nullable
    private Boolean sound;
    @Nullable
    private Integer volume;
    @Nullable
    private Boolean vibrations;
    @Nullable
    private Integer vibrationDuration;
    @Nullable
    private Integer orientation;
    @Nullable
    private Boolean randomizeWordOrder;

    /**
     * Instantiates a new Remote preferences.
     *
     * @param sizePortrait      see {@link #getSizePortrait()}
     * @param sizeLandscape     see {@link #getSizeLandscape()}
     * @param sound             see {@link #getSound()}
     * @param volume            see {@link #getVolume()}
     * @param vibrations        see {@link #getVibrations()}
     * @param vibrationDuration see {@link #getVibrationDuration()}
     * @param orientation       see {@link #getOrientation()}
     */
    public RemotePreferences(@Nullable Integer sizePortrait, @Nullable Integer sizeLandscape, @Nullable Boolean sound, @Nullable Integer volume, @Nullable Boolean vibrations, @Nullable Integer vibrationDuration, @Nullable Integer orientation) {
        this.sizePortrait = sizePortrait;
        this.sizeLandscape = sizeLandscape;
        this.sound = sound;
        this.volume = volume;
        this.vibrations = vibrations;
        this.vibrationDuration = vibrationDuration;
        this.orientation = orientation;
    }

    /**
     * Gets sizePortrait.
     *
     * @return The size (height) of keyboard in portrait mode. A percent value of device's screen height.
     */
    @Nullable
    public Integer getSizePortrait() {
        return sizePortrait;
    }

    /**
     * Gets size landscape.
     *
     * @return The size (height) of keyboard in landscape mode. A percent value of device's screen height.
     */
    @Nullable
    public Integer getSizeLandscape() {
        return sizeLandscape;
    }

    /**
     * Gets sound.
     *
     * @return True - enable sounds (keyboard button touch, test session error), false - disable sounds.
     */
    @Nullable
    public Boolean getSound() {
        return sound;
    }

    /**
     * Gets volume.
     *
     * @return The volume of sound ({@link #getSound()}).
     */
    @Nullable
    public Integer getVolume() {
        return volume;
    }

    /**
     * Gets vibrations.
     *
     * @return True - enable vibrations (keyboard button touch, test session error), false - disable vibrations.
     */
    @Nullable
    public Boolean getVibrations() {
        return vibrations;
    }

    /**
     * Gets vibrationDuration.
     *
     * @return The vibration duration for keyboard button press (in milliseconds).
     */
    @Nullable
    public Integer getVibrationDuration() {
        return vibrationDuration;
    }

    /**
     * Gets orientation.
     *
     * @return Desired screen orientation for test session - {@link #ORIENTATION_PORTRAIT} or {@link #ORIENTATION_LANDSCAPE}.
     */
    @Nullable
    public Integer getOrientation() {
        return orientation;
    }

    /**
     * Gets remote preferences from remote json file.
     *
     * @param url The url of the json file.
     * @return The instance of RemotePreferences based on remote json file.
     * @throws IOException any I/O error when downloading and parsing json file
     */
    public static RemotePreferences fromUrl(String url) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(new InputStreamReader(new URL(url).openStream()), RemotePreferences.class);
    }

    @Nullable
    public Boolean getRandomizeWordOrder() {
        return randomizeWordOrder;
    }
}
