package com.example.taptimingkeyboard.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_info")
public class UserInfo {

    public static final String SEX_MALE="male";
    public static final String SEX_FEMALE="female";
    public static final String HANDEDNESS_LEFT="left";
    public static final String HANDEDNESS_RIGHT="right";
    public static final String ASYMMETRY_LEFT="left";
    public static final String ASYMMETRY_RIGHT="right";
    public static final String ASYMMETRY_NO_ASYMMETRY ="not asymmetric";
    public static final String ASYMMETRY_NOT_SPECIFIED="not specified";


    @PrimaryKey(autoGenerate = true)
    private long id;
    private String firstName;
    private String lastName;
    private int age;
    private String sex;
    private String handedness;
    private boolean diagnosedWithPD;
    private String symptomsAsymmetry;
    private boolean onMedication;

    public UserInfo(String firstName, String lastName, int age, String sex, String handedness, boolean diagnosedWithPD, String symptomsAsymmetry, boolean onMedication) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.sex = sex;
        this.handedness = handedness;
        this.diagnosedWithPD = diagnosedWithPD;
        this.symptomsAsymmetry = symptomsAsymmetry;
        this.onMedication = onMedication;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public String getSex() {
        return sex;
    }

    public String getHandedness() {
        return handedness;
    }

    public boolean isDiagnosedWithPD() {
        return diagnosedWithPD;
    }

    public String getSymptomsAsymmetry() {
        return symptomsAsymmetry;
    }

    public boolean isOnMedication() {
        return onMedication;
    }
}
