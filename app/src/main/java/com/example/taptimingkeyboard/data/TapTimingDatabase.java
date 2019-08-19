package com.example.taptimingkeyboard.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FlightTimeCharacteristics.class,KeyTapCharacteristics.class, TestSession.class}, version = 11)
public abstract class TapTimingDatabase extends RoomDatabase {

    public static final String DB_NAME = "tt_database";

    private static TapTimingDatabase db = null;

    public static TapTimingDatabase instance(Context applicationContext) {
        if(db!=null)
            return db;
        else {
            db = Room.databaseBuilder(applicationContext, TapTimingDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
            return db;
        }
    }

    public abstract TestSessionDao testSessionDao();
    public abstract FlightTimeCharacteristicsDao flightTimeCharacteristicsDao();
    public abstract KeyTapCharacteristicsDao keyTapCharacteristicsDao();

}