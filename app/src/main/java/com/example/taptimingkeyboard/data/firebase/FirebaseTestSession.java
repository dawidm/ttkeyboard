package com.example.taptimingkeyboard.data.firebase;

import com.example.taptimingkeyboard.data.FlightTimeCharacteristics;
import com.example.taptimingkeyboard.data.KeyTapCharacteristics;
import com.example.taptimingkeyboard.data.TestSession;
import com.example.taptimingkeyboard.data.TestSessionWordErrors;
import com.example.taptimingkeyboard.data.UserInfo;

import java.util.List;

public class FirebaseTestSession extends TestSession {

    private String firebaseInstanceId;
    private UserInfo userInfo;
    private List<KeyTapCharacteristics> keyTapCharacteristics;
    private List<FlightTimeCharacteristics> flightTimeCharacteristics;
    private List<TestSessionWordErrors> wordErrors;

    public FirebaseTestSession(TestSession testSession, String firebaseInstanceId) {
        super(testSession.getUserId(), testSession.getTimestampMs(), testSession.getWordlistName(), testSession.getWordlistWordsMd5Hash(), testSession.getOrientationLandscape(), testSession.getPhoneInfo(), testSession.getPhoneXDpi(), testSession.getPhoneYDpi());
        this.setId(testSession.getId());
        this.setNumErrors(testSession.getNumErrors());
        this.setSessionEndTimestampMs(testSession.getSessionEndTimestampMs());
        this.setSynchronized(testSession.isSynchronized());
        this.firebaseInstanceId=firebaseInstanceId;
    }

    public String getFirebaseInstanceId() {
        return firebaseInstanceId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public List<KeyTapCharacteristics> getKeyTapCharacteristics() {
        return keyTapCharacteristics;
    }

    public List<FlightTimeCharacteristics> getFlightTimeCharacteristics() {
        return flightTimeCharacteristics;
    }

    public List<TestSessionWordErrors> getWordErrors() {
        return wordErrors;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setKeyTapCharacteristics(List<KeyTapCharacteristics> keyTapCharacteristics) {
        this.keyTapCharacteristics = keyTapCharacteristics;
    }

    public void setFlightTimeCharacteristics(List<FlightTimeCharacteristics> flightTimeCharacteristics) {
        this.flightTimeCharacteristics = flightTimeCharacteristics;
    }

    public void setWordErrors(List<TestSessionWordErrors> wordErrors) {
        this.wordErrors = wordErrors;
    }

}
