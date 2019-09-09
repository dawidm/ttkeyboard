package com.example.taptimingkeyboard.activity;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.tools.LimitedFrequencyExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Plays sounds and vibration sequences
 */
public class UiSounds {

    //positive numbers for sounds, negative for vibrations
    public static final int SOUND_WORD_ERROR=1;
    public static final int VIBRATION_WORD_ERROR=-1;

    private static final int VIBRATION_WORD_ERROR_DURATION=300;

    private Context activityContext;

    private SoundPool soundPool;
    private Map<Integer,Integer> errorCodeToSoundIdMap = new HashMap<>();
    private AudioManager audioManager;
    private Vibrator vibrator;
    private LimitedFrequencyExecutor limitedFrequencyExecutor = new LimitedFrequencyExecutor();

    /**
     * @param context The context of activity or service using this class.
     */
    public UiSounds(Context context) {
        this.activityContext=context;
    }

    /**
     * Prepare to play sounds. Use to minimize latency when using playSound for the first time.
     */
    public void initSounds() {
        soundPool = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build()).setMaxStreams(1).build();
        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_RING,0);
        errorCodeToSoundIdMap.put(SOUND_WORD_ERROR,soundPool.load(activityContext, R.raw.beep_short,1));
    }

    /**
     * Prepare to play "click sound". Use to minimize latency when using playClickSound for the first time.
     */
    public void initClickSound() {
        if (audioManager == null)
            audioManager = (AudioManager)activityContext.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Prepare to use vibrations. Use to minimize latency when using vibrations for the first time.
     */
    public void initVibrator() {
        if(vibrator==null)
            vibrator = (Vibrator)activityContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Plays specified sound.
     * @param sound The sound - ({@link UiSounds} constant variable beginning with SOUND)
     * @param volume Volume 0 to 1.
     */
    public void playSound(final int sound, final float volume) {
        if(soundPool==null)
            initSounds();
        soundPool.play(errorCodeToSoundIdMap.get(sound), volume, volume, 0, 0, 1);
    }

    /**
     * Play specified sound, but do nothing if a timeout for previous play for this sound hasn't passed.
     * @param sound The sound - ({@link UiSounds} constant variable beginning with SOUND)
     * @param volume Volume 0 to 1.
     * @param timeoutMs The timeouts in milliseconds.
     */
    public void playSound(final int sound, final float volume, final int timeoutMs) {
        if(limitedFrequencyExecutor.canRunNow(sound)) {
            limitedFrequencyExecutor.run(sound, new Runnable() {
                @Override
                public void run() {
                    playSound(sound,volume);
                }
            },timeoutMs);
        }
    }

    /**
     * Play Android's standard click sound.
     * @param clickVol Volume 0 to 1.
     */
    public void playClickSound(final float clickVol) {
        if (audioManager == null)
            initClickSound();
        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD,clickVol);
    }

    /**
     * Vibrate for a specified time.
     * @param durationMs The time in milliseconds.
     */
    public void vibrateMs(final int durationMs) {
        if(vibrator==null) {
            initVibrator();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs,VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(durationMs);
        }
    }

    /**
     * Vibrate for predefined amount of time or using predefined sequence.
     * @param vibration The predefined amount of time or sequence. {@link UiSounds} constant variable beginning with VIBRATION.
     */
    public void vibrate(final int vibration) {
        vibrateMs(getVibrationDurationMs(vibration));
    }


    /**
     * Vibrate for predefined amount of time or using predefined sequence, but do nothing if a timeout for previous play of this predefined vibration hasn't passed.
     * @param vibration The predefined amount of time or sequence. {@link UiSounds} constant variable beginning with VIBRATION.
     */
    public void vibrate(final int vibration, final int timeoutMs) {
        if(limitedFrequencyExecutor.canRunNow(vibration))
            limitedFrequencyExecutor.run(vibration, new Runnable() {
                @Override
                public void run() {
                    vibrate(vibration);
                }
            },timeoutMs);
    }

    private int getVibrationDurationMs(final int vibration) {
        switch (vibration) {
            case VIBRATION_WORD_ERROR:
                return VIBRATION_WORD_ERROR_DURATION;
                default:
                    throw new RuntimeException("wrong vibration code: " + vibration);
        }
    }

}
