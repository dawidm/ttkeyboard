package com.example.taptimingkeyboard;

import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimingDataManager {

    public static final String TAG = TimingDataManager.class.getName();
    public static final long CHECK_ACCEPTED_CLICKS_INTERVAL_MS=100;

    private Context context;
    private boolean testSessionMode = false;

    //key = clickId
    private Map<Long,KeyTapCharacteristics> keyTapCharacteristicsQueue = Collections.synchronizedMap(new HashMap<Long, KeyTapCharacteristics>());
    //key = clickId of the second key
    private Map<Long,FlightTimeCharacteristics> flightTimeCharacteristicsBySecondId = Collections.synchronizedMap(new HashMap<Long, FlightTimeCharacteristics>());
    //key = clickId of the first key
    private Map<Long,FlightTimeCharacteristics> flightTimeCharacteristicsByFirstId = Collections.synchronizedMap(new HashMap<Long, FlightTimeCharacteristics>());
    //first of second clickId has been accepted TODO hashCode
    private Set<FlightTimeCharacteristics> flightTimeCharacteristicsAcceptedOnce = Collections.synchronizedSet(new HashSet<FlightTimeCharacteristics>());

    private Set<Long> acceptedClickIds = Collections.synchronizedSet(new HashSet<Long>());
    private Set<Long> rejectedClickIds = Collections.synchronizedSet(new HashSet<Long>());

    private ScheduledExecutorService scheduledExecutorService;

    public TimingDataManager(Context context) {
        this.context = context;
    }

    public void startTestSession() {
        testSessionMode=true;
        scheduledExecutorService=Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    checkAcceptedClicks();
                } catch (Exception e) {
                    Log.w(TAG,e);
                }
            }
        },CHECK_ACCEPTED_CLICKS_INTERVAL_MS,CHECK_ACCEPTED_CLICKS_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void checkAcceptedClicks() {
        boolean clicksChecked=false;
        if(!acceptedClickIds.isEmpty()) {
            clicksChecked=true;
            Iterator<Long> acceptedClicksIterator = acceptedClickIds.iterator();
            while (acceptedClicksIterator.hasNext()) {
                Long key = acceptedClicksIterator.next();
                if(keyTapCharacteristicsQueue.containsKey(key)) {
                    Log.d(TAG,"inserting keyTapCharacteristics " + keyTapCharacteristicsQueue.get(key).getKeyCharacter());
                    TapTimingDatabase.instance(context).keyTapCharacteristicsDao().insertAll(keyTapCharacteristicsQueue.get(key));
                    keyTapCharacteristicsQueue.remove(key);
                }
                if(flightTimeCharacteristicsByFirstId.containsKey(key)) {
                    if(flightTimeCharacteristicsAcceptedOnce.contains(flightTimeCharacteristicsByFirstId.get(key))) {
                        flightTimeCharacteristicsAcceptedOnce.remove(flightTimeCharacteristicsByFirstId.get(key));
                        Log.d(TAG,"inserting flightTimeCharacteristics " + flightTimeCharacteristicsByFirstId.get(key).getCharFrom() + "->" + flightTimeCharacteristicsByFirstId.get(key).getCharTo());
                        TapTimingDatabase.instance(context).flightTimeCharacteristicsDao().insertAll(flightTimeCharacteristicsByFirstId.get(key));
                    } else {
                        flightTimeCharacteristicsAcceptedOnce.add(flightTimeCharacteristicsByFirstId.get(key));
                    }
                    flightTimeCharacteristicsByFirstId.remove(key);
                }
                if(flightTimeCharacteristicsBySecondId.containsKey(key)) {
                    if(flightTimeCharacteristicsAcceptedOnce.contains(flightTimeCharacteristicsBySecondId.get(key))) {
                        flightTimeCharacteristicsAcceptedOnce.remove(flightTimeCharacteristicsBySecondId.get(key));
                        Log.d(TAG,"inserting flightTimeCharacteristics " + flightTimeCharacteristicsBySecondId.get(key).getCharFrom() + "->" + flightTimeCharacteristicsBySecondId.get(key).getCharTo());
                        TapTimingDatabase.instance(context).flightTimeCharacteristicsDao().insertAll(flightTimeCharacteristicsBySecondId.get(key));
                    } else {
                        flightTimeCharacteristicsAcceptedOnce.add(flightTimeCharacteristicsBySecondId.get(key));
                    }
                    flightTimeCharacteristicsBySecondId.remove(key);
                }
                acceptedClicksIterator.remove();
            }
        }
        if (!rejectedClickIds.isEmpty()) {
            clicksChecked=true;
            Iterator<Long> rejectedClicksIterator = rejectedClickIds.iterator();
            while (rejectedClicksIterator.hasNext()) {
                Long key = rejectedClicksIterator.next();
                keyTapCharacteristicsQueue.remove(key);
                flightTimeCharacteristicsByFirstId.remove(key);
                flightTimeCharacteristicsBySecondId.remove(key);
                flightTimeCharacteristicsAcceptedOnce.remove(key);
                rejectedClicksIterator.remove();
            }
        }
        if(clicksChecked)
            Log.d(TAG,String.format("ended checking accepted/rejected clicks; " +
                            "keyTapCharacteristicsQueue size: %d, " +
                            "flightTimeCharacteristicsByFirstId size: %d, " +
                            "flightTimeCharacteristicsBySecondId size: %d, " +
                            "flightTimeCharacteristicsAcceptedOnce size: %d",
                    keyTapCharacteristicsQueue.size(),
                    flightTimeCharacteristicsByFirstId.size(),
                    flightTimeCharacteristicsBySecondId.size(),
                    flightTimeCharacteristicsAcceptedOnce.size()));
    }

    public void endTestSession() {
        testSessionMode=false;
        if(scheduledExecutorService!=null && !scheduledExecutorService.isShutdown())
            scheduledExecutorService.shutdown();
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkAcceptedClicks();
            }
        }).start();
    }

    public void addKeyTapCharacteristics(final KeyTapCharacteristics keyTapCharacteristics, final long clickId) {
        Log.d(TAG,"keyTapCharacteristics: " + keyTapCharacteristics.getKeyCharacter());
        if(!testSessionMode) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TapTimingDatabase.instance(context).keyTapCharacteristicsDao().insertAll(keyTapCharacteristics);
                }
            }).start();
        } else {
            keyTapCharacteristicsQueue.put(clickId,keyTapCharacteristics);
        }
    }

    public void addFlightTimeCharacteristics(final FlightTimeCharacteristics flightTimeCharacteristics, final long firstClickId, final long secondClickId) {
        Log.d(TAG,"flightTimeCharacteristics: " + flightTimeCharacteristics.getCharFrom() + "->" + flightTimeCharacteristics.getCharTo());
        if(!testSessionMode) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TapTimingDatabase.instance(context).flightTimeCharacteristicsDao().insertAll(flightTimeCharacteristics);
                }
            }).start();
        } else {
            flightTimeCharacteristicsByFirstId.put(firstClickId,flightTimeCharacteristics);
            flightTimeCharacteristicsBySecondId.put(secondClickId,flightTimeCharacteristics);
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
