package com.example.taptimingkeyboard.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TestSessionWordErrorsDao {

    @Insert
    void insertAll(TestSessionWordErrors... testSessionWordErrors);

    @Query("SELECT * FROM word_errors WHERE sessionId == :sessionId")
    TestSessionWordErrors[] getForSession(long sessionId);

}
