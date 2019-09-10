package com.example.taptimingkeyboard.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Application database (Android Room) for storing user, user interaction and test session data.
 */
@Database(entities = {FlightTimeCharacteristics.class,KeyTapCharacteristics.class, TestSession.class, TestSessionWordErrors.class, UserInfo.class}, version = 22)
@TypeConverters(Converters.class)
public abstract class TapTimingDatabase extends RoomDatabase {

    public static final String DB_NAME = "tt_database";

    private static TapTimingDatabase db = null;

    /**
     * Get TapTimingDatabase singleton.
     *
     * @param applicationContext application context
     * @return TapTimingDatabase instance
     */
    public static TapTimingDatabase instance(Context applicationContext) {
        if(db!=null)
            return db;
        else {
            db = buildDatabase(applicationContext);
            return db;
        }
    }

    /**
     * Creates TapTimingDatabase instance.
     * Adds callback to populate user_info table with "default" user when database is created
     *
     * @param context application context
     * @return TapTimingDatabase instance
     */
    private static TapTimingDatabase buildDatabase(final Context context) {
        return Room.databaseBuilder(context,
                TapTimingDatabase.class,
                DB_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                instance(context).userInfoDao().insert(UserInfo.defaultUser());
                            }
                        });
                    }
                })
                .fallbackToDestructiveMigration().build();
    }

    public abstract TestSessionDao testSessionDao();
    public abstract FlightTimeCharacteristicsDao flightTimeCharacteristicsDao();
    public abstract KeyTapCharacteristicsDao keyTapCharacteristicsDao();
    public abstract UserInfoDao userInfoDao();
    public abstract TestSessionWordErrorsDao testSessionWordErrorsDao();

}