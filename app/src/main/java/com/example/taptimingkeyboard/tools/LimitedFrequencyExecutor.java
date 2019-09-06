package com.example.taptimingkeyboard.tools;

import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs tasks (Runnables) with limited frequency.
 * The task is a Runnable with specified id. No runnable with same id can be executed until the time specified in previous execution pass.
 */
public class LimitedFrequencyExecutor {

    private Map<Integer,Long> idDelayMillisMap = new HashMap<>();
    private Map<Integer,Long> idLastRunTimeMillisMap = new HashMap<>();

    /**
     * Check if the delay time for previous execution with this id has passed.
     * @param id The id.
     * @return Whether the time passed.
     */
    public boolean canRunNow(int id) {
        Long delayMillis = idDelayMillisMap.get(id);
        Long lastRunTimeMillis = idLastRunTimeMillisMap.get(id);
        if(delayMillis==null)
            return true;
        if(SystemClock.elapsedRealtime()>lastRunTimeMillis+delayMillis)
            return true;
        else
            return false;
    }

    /**
     * Run specified task (Runnable) if the delay time for previous task with this id has passed.
     * @param id The id.
     * @param runnable The Runnable to run.
     * @param delayTillNextRun The time that should pass until the next execution of task with the same id.
     * @return Whether the task (Runnable) has been executed.
     */
    public boolean run(int id, Runnable runnable, long delayTillNextRun) {
        if (canRunNow(id)) {
            idDelayMillisMap.put(id,delayTillNextRun);
            idLastRunTimeMillisMap.put(id,SystemClock.elapsedRealtime());
            runnable.run();
            return true;
        } else
            return false;
    }
}
