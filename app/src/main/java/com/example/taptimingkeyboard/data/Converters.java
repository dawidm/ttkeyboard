package com.example.taptimingkeyboard.data;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static UserInfo.Sex sexFromString(String value) {
        if(value==null)
            return null;
        return UserInfo.Sex.valueOf(value);
    }

    @TypeConverter
    public static String stringFromSex(UserInfo.Sex value) {
        if(value==null)
            return null;
        return value.name();
    }
    @TypeConverter
    public static UserInfo.Handedness handednessFromString(String value) {
        if(value==null)
            return null;
        return UserInfo.Handedness.valueOf(value);
    }

    @TypeConverter
    public static String stringFromHandedness(UserInfo.Handedness value) {
        if(value==null)
            return null;
        return value.name();
    }
    @TypeConverter
    public static UserInfo.Asymmetry asymmetryFromString(String value) {
        if(value==null)
            return null;
        return UserInfo.Asymmetry.valueOf(value);
    }

    @TypeConverter
    public static String stringFromAsymmetry(UserInfo.Asymmetry value) {
        if(value==null)
            return null;
        return value.name();
    }
}