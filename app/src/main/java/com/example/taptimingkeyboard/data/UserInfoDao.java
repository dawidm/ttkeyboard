package com.example.taptimingkeyboard.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

@Dao
public interface UserInfoDao {

        @Insert
        void insertAll(UserInfo... userInfos);

        @Delete
        void delete(UserInfo userInfo);
}
