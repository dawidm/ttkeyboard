package com.example.taptimingkeyboard.activity;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.example.taptimingkeyboard.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UiSounds {

    public static final int SOUND_WORD_ERROR=0;
    public static final int VIBRATION_WORD_ERROR=0;

    private static final int VIBRATION_WORD_ERROR_DURATION=300;

    private Context activityContext;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private Map<Integer,Integer> errorCodeToSoundIdMap = new HashMap<>();
    private Map<Integer, ScheduledFuture> soundTimeoutScheduledFutureMap = new HashMap<>();
    private Map<Integer, ScheduledFuture> vibrationTimeoutScheduledFutureMap = new HashMap<>();

    private SoundPool soundPool;
    private AudioManager audioManager;
    private Vibrator vibrator;

    public UiSounds(Context context) {
        this.activityContext=context;
    }

    public void initSounds() {
        soundPool = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build()).setMaxStreams(1).build();
        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_RING,0);
        errorCodeToSoundIdMap.put(SOUND_WORD_ERROR,soundPool.load(activityContext, R.raw.beep_short,1));
    }

    public void initClickSound() {
        if (audioManager == null)
            audioManager = (AudioManager)activityContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void initVibrator() {
        if(vibrator==null)
            initVibrator();
    }

    public void playSound(int sound, float volume) {
        if(soundPool==null)
            initSounds();
        soundPool.play(errorCodeToSoundIdMap.get(sound), volume, volume, 0, 0, 1);
    }

    public void playSound(int sound, float volume, int timeoutMs) {
        ScheduledFuture currentSoundScheduledFuture = soundTimeoutScheduledFutureMap.get(sound);
        if(currentSoundScheduledFuture==null || (currentSoundScheduledFuture!=null && currentSoundScheduledFuture.isDone())) {
            playSound(sound,volume);
            soundTimeoutScheduledFutureMap.put(sound,scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {

                }
            }, timeoutMs, TimeUnit.MILLISECONDS));
        }
    }

    public void playClickSound(float clickVol) {
        if (audioManager == null)
            initClickSound();
        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD,clickVol);
    }

    public void vibrateMs(int durationMs) {
        if(vibrator==null) {
            vibrator = (Vibrator)activityContext.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs,VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(durationMs);
        }
    }

    public void vibrate(int vibration) {
        vibrateMs(getVibrationDurationMs(vibration));
    }

    public void vibrate(int vibration, int timeoutMs) {
        ScheduledFuture currentVibrationScheduledFuture = vibrationTimeoutScheduledFutureMap.get(vibration);
        if(currentVibrationScheduledFuture==null || (currentVibrationScheduledFuture!=null && currentVibrationScheduledFuture.isDone())) {
            vibrate(vibration);
            vibrationTimeoutScheduledFutureMap.put(vibration,scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {

                }
            }, timeoutMs, TimeUnit.MILLISECONDS));
        }
    }

    private int getVibrationDurationMs(int vibration) {
        switch (vibration) {
            case VIBRATION_WORD_ERROR:
                return VIBRATION_WORD_ERROR_DURATION;
                default:
                    throw new RuntimeException("wrong vibration code: " + vibration);
        }
    }

}
