package com.example.taptimingkeyboard.data;

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

    public RemotePreferences(@Nullable Integer sizePortrait, @Nullable Integer sizeLandscape, @Nullable Boolean sound, @Nullable Integer volume) {
        this.sizePortrait = sizePortrait;
        this.sizeLandscape = sizeLandscape;
        this.sound = sound;
        this.volume = volume;
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

    public static RemotePreferences fromUrl(String url) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(new InputStreamReader(new URL(url).openStream()), RemotePreferences.class);
    }

}
