package com.example.taptimingkeyboard;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO check clickids by scheduled executor
public class TimingDataManager {

    public static final String TAG = TimingDataManager.class.getName();

    private Context context;
    private boolean testSessionMode = false;
    private long sessionId;

    //key = clickId
    private Map<Long,KeyTapCharacteristics> keyTapCharacteristicsQueue = new HashMap<>();
    //key = clickId of the second key
    private Map<Long,FlightTimeCharacteristics> flightTimeCharacteristicsSecondId = new HashMap<>();
    //key = clickId of the first key
    private Map<Long,FlightTimeCharacteristics> flightTimeCharacteristicsFirstId = new HashMap<>();

    private Set<Long> acceptedClickIds = new HashSet<>();
    private Set<Long> rejectedClickIds = new HashSet<>();

    public TimingDataManager(Context context) {
        this.context = context;
    }

    public void startTestSession(long sessionId) {
        testSessionMode=true;
        this.sessionId=sessionId;
    }

    public void endTestSession() {
        testSessionMode=false;
    }

    public void addKeyTapCharacteristics(KeyTapCharacteristics keyTapCharacteristics, long clickId) {
        Log.d(TAG,"keyTapCharacteristics: " + keyTapCharacteristics.getKeyCharacter());
        if(!testSessionMode) {
            TapTimingDatabase.instance(context).keyTapCharacteristicsDao().insertAll(keyTapCharacteristics);
        } else {
            keyTapCharacteristicsQueue.put(clickId,keyTapCharacteristics);
        }
    }

    public void addFlightTimeCharacteristics(FlightTimeCharacteristics flightTimeCharacteristics, long firstClickId, long secondClickId) {
        Log.d(TAG,"flightTimeCharacteristics: " + flightTimeCharacteristics.getCharFrom() + "->" + flightTimeCharacteristics.getCharTo());
        if(!testSessionMode) {
            TapTimingDatabase.instance(context).flightTimeCharacteristicsDao().insertAll(flightTimeCharacteristics);
        } else {
            flightTimeCharacteristicsFirstId.put(firstClickId,flightTimeCharacteristics);
            flightTimeCharacteristicsSecondId.put(secondClickId,flightTimeCharacteristics);
        }
    }

    public void acceptButtonClick(long clickId) {
        Log.d(TAG, "test session accepted click id: " + clickId);
        acceptedClickIds.add(clickId);
    }

    public void rejectButtonClick(long clickId) {
        Log.d(TAG, "test session rejected click id: " + clickId);
        rejectedClickIds.add(clickId);
    }

}
