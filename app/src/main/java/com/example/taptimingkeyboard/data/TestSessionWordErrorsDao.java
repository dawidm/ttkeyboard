package com.example.taptimingkeyboard.data;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface TestSessionWordErrorsDao {

    @Insert
    void insertAll(TestSessionWordErrors... testSessionWordErrors);

}
