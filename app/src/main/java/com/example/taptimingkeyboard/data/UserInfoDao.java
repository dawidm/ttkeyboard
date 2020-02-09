package com.example.taptimingkeyboard.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserInfoDao {

        @Insert
        long insert(UserInfo userInfo);

        @Delete
        void delete(UserInfo userInfo);

        @Query("SELECT * FROM user_info ;")
        UserInfo[] getAll();

        @Query("SELECT * FROM user_info WHERE id == :id")
        UserInfo getById(long id);

        @Query("SELECT * FROM user_info WHERE id != 1")
        UserInfo[] getAllButDefault();

        @Query("SELECT * FROM user_info WHERE email == :email")
        UserInfo[] getByEmail(String email);
}
