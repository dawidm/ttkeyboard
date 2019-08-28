package com.example.taptimingkeyboard.tools;

import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;

public class LimitedFrequencyExecutor {

    private Map<Integer,Long> idDelayMillisMap = new HashMap<>();
    private Map<Integer,Long> idLastRunTimeMillisMap = new HashMap<>();

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
