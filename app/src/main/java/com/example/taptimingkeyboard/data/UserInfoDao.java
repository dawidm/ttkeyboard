package com.example.taptimingkeyboard.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

@Dao
public interface UserInfoDao {

        @Insert
        long insert(UserInfo userInfo);

        @Delete
        void delete(UserInfo userInfo);
}
