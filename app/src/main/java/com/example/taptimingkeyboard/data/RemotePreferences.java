package com.example.taptimingkeyboard.data;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class RemotePreferences {
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

    public RemotePreferences(@Nullable Integer sizePortrait, @Nullable Integer sizeLandscape, @Nullable Boolean sound, @Nullable Integer volume, @Nullable Boolean vibrations, @Nullable Integer vibrationDuration) {
        this.sizePortrait = sizePortrait;
        this.sizeLandscape = sizeLandscape;
        this.sound = sound;
        this.volume = volume;
        this.vibrations = vibrations;
        this.vibrationDuration = vibrationDuration;
    }

    @Nullable
    public Integer getSizePortrait() {
        return sizePortrait;
    }

    @Nullable
    public Integer getSizeLandscape() {
        return sizeLandscape;
    }

    @Nullable
    public Boolean getSound() {
        return sound;
    }

    @Nullable
    public Integer getVolume() {
        return volume;
    }

    @Nullable
    public Boolean getVibrations() {
        return vibrations;
    }

    @Nullable
    public Integer getVibrationDuration() {
        return vibrationDuration;
    }

    public static RemotePreferences fromUrl(String url) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(new InputStreamReader(new URL(url).openStream()), RemotePreferences.class);
    }

}
