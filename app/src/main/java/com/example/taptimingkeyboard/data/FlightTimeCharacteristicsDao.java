package com.example.taptimingkeyboard.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface FlightTimeCharacteristicsDao {

    @Insert
    void insertAll(FlightTimeCharacteristics... flightTimeCharacteristics);

    @Delete
    void delete(FlightTimeCharacteristics flightTimeCharacteristics);

    @Query("SELECT * FROM flight_time_characteristics WHERE sessionId == :sessionId")
    FlightTimeCharacteristics[] getForSession(long sessionId);

}
