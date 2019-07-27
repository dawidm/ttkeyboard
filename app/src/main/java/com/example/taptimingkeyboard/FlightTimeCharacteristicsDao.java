package com.example.taptimingkeyboard;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

@Dao
public interface FlightTimeCharacteristicsDao {

    @Insert
    void insertAll(FlightTimeCharacteristics... flightTimeCharacteristics);

    @Delete
    void delete(FlightTimeCharacteristics flightTimeCharacteristics);

}
