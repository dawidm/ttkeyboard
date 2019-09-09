package com.example.taptimingkeyboard.data;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Stores information about keyboard user.
 */
@Entity(tableName = "user_info")
public class UserInfo {

    /**
     * The constant DEFAULT_USER_NAME.
     */
    public static final String DEFAULT_USER_NAME="default_user";

    /**
     * The constant SEX_MALE.
     */
    public static final String SEX_MALE="male";
    /**
     * The constant SEX_FEMALE.
     */
    public static final String SEX_FEMALE="female";
    /**
     * The constant HANDEDNESS_LEFT.
     */
    public static final String HANDEDNESS_LEFT="left";
    /**
     * The constant HANDEDNESS_RIGHT.
     */
    public static final String HANDEDNESS_RIGHT="right";
    /**
     * The constant ASYMMETRY_LEFT.
     */
    public static final String ASYMMETRY_LEFT="left";
    /**
     * The constant ASYMMETRY_RIGHT.
     */
    public static final String ASYMMETRY_RIGHT="right";
    /**
     * The constant ASYMMETRY_NO_ASYMMETRY.
     */
    public static final String ASYMMETRY_NO_ASYMMETRY ="not asymmetric";
    /**
     * The constant ASYMMETRY_NOT_SPECIFIED.
     */
    public static final String ASYMMETRY_NOT_SPECIFIED="not specified";


    @PrimaryKey(autoGenerate = true)
    private long id;
    private String firstName;
    private String lastName;
    private int age;
    private String sex;
    private String handedness;
    private boolean diagnosedWithPD;
    @Nullable
    private String symptomsAsymmetry;
    @Nullable
    private Boolean onMedication;

    /**
     * Instantiates a new User info.
     *
     * @param firstName         see {@link #getFirstName()}
     * @param lastName          see {@link #getLastName()}
     * @param age               see {@link #getAge()}
     * @param sex               see {@link #getSex()}
     * @param handedness        see {@link #getHandedness()}
     * @param diagnosedWithPD   see {@link #diagnosedWithPD}
     * @param symptomsAsymmetry see {@link #symptomsAsymmetry}
     * @param onMedication      see {@link #getOnMedication()}
     */
    public UserInfo(String firstName, String lastName, int age, String sex, String handedness, boolean diagnosedWithPD, @Nullable String symptomsAsymmetry, @Nullable Boolean onMedication) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.sex = sex;
        this.handedness = handedness;
        this.diagnosedWithPD = diagnosedWithPD;
        this.symptomsAsymmetry = symptomsAsymmetry;
        this.onMedication = onMedication;
    }

    /**
     * Sets id.
     *
     * @param id see {@link #getId()}
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets id.
     *
     * @return The unique id (auto generated by Room Library)
     */
    public long getId() {
        return id;
    }

    /**
     * Gets first name.
     *
     * @return The first name of the keyboard user.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets last name.
     *
     * @return The last name of the keyboard user.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets age.
     *
     * @return The age of the keyboard user.
     */
    public int getAge() {
        return age;
    }

    /**
     * Gets sex.
     *
     * @return The sex of the keyboard user, {@link #SEX_MALE} or {@link #SEX_FEMALE}
     */
    public String getSex() {
        return sex;
    }

    /**
     * Gets handedness.
     *
     * @return The handedness of the keyboard user, {@link #HANDEDNESS_RIGHT} or {@link #HANDEDNESS_LEFT}
     */
    public String getHandedness() {
        return handedness;
    }

    /**
     * Gets isDiagnosedWithPD
     *
     * @return Whether the user is diagnosed with Parkinson's disease
     */
    public boolean isDiagnosedWithPD() {
        return diagnosedWithPD;
    }

    /**
     * Gets symptoms asymmetry.
     *
     * @return User's Parkinson's disease symptoms asymmetry {@link #ASYMMETRY_RIGHT}, {@link #ASYMMETRY_LEFT}, {@link #ASYMMETRY_NOT_SPECIFIED} or {@link #ASYMMETRY_NOT_SPECIFIED}
     */
    public String getSymptomsAsymmetry() {
        return symptomsAsymmetry;
    }

    /**
     * Gets onMedication.
     *
     * @return Whether the user user takes any Parkinson's disease medication.
     */
    @Nullable
    public Boolean getOnMedication() {
        return onMedication;
    }

    @Override
    public String toString() {
        if(this.getFirstName().equals(DEFAULT_USER_NAME))
            return DEFAULT_USER_NAME;
        else
            return firstName+" "+lastName+", id: " + id;
    }

    /**
     * Default UserInfo
     *
     * @return The instance of UserInfo to be used by application when no other users were added.
     */
    public static UserInfo defaultUser() {
        return new UserInfo(DEFAULT_USER_NAME,"",0,SEX_MALE,HANDEDNESS_RIGHT,false,null,null);
    }
}
