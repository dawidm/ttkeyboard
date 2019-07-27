package com.example.taptimingkeyboard;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

@Dao
public interface KeyTapCharacteristicsDao {

    @Insert
    void insertAll(KeyTapCharacteristics... keyTapCharacteristics);

    @Delete
    void delete(KeyTapCharacteristics keyTapCharacteristics);

}
