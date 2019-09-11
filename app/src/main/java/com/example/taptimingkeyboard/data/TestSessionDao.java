package com.example.taptimingkeyboard.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TestSessionDao {

    @Insert
    long insert(TestSession testSession);

    @Delete
    void delete(TestSession testSession);

    @Update
    void update(TestSession testSession);

    @Query("SELECT * FROM test_sessions WHERE id == :id")
    TestSession getById(long id);

    @Query("SELECT * FROM test_sessions")
    TestSession[] getAll();

}
