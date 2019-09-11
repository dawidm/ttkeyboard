package com.example.taptimingkeyboard.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface KeyTapCharacteristicsDao {

    @Insert
    void insertAll(KeyTapCharacteristics... keyTapCharacteristics);

    @Delete
    void delete(KeyTapCharacteristics keyTapCharacteristics);

    @Query("SELECT * FROM key_tap_characteristics WHERE sessionId == :sessionId")
    KeyTapCharacteristics[] getForSession(long sessionId);

}
