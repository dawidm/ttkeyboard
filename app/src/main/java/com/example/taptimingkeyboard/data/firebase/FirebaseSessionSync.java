package com.example.taptimingkeyboard.data.firebase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.taptimingkeyboard.data.FlightTimeCharacteristics;
import com.example.taptimingkeyboard.data.KeyTapCharacteristics;
import com.example.taptimingkeyboard.data.TapTimingDatabase;
import com.example.taptimingkeyboard.data.TestSession;
import com.example.taptimingkeyboard.data.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

public class FirebaseSessionSync {

    public interface OnSuccessfulSyncListener {
        void onSuccessfulSync();
    }

    public interface OnSyncFailureListener {
        void onSyncFailure(Exception e);
    }

    public static final String TAG = FirebaseSessionSync.class.getName();

    private static final String FIREBASE_COLLECTION_NAME = "test_sessions";

    private Context applicationContext;
    private FirebaseFirestore firestore;

    public FirebaseSessionSync(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void syncSession(final long sessionId, final OnSuccessfulSyncListener onSuccessfulSyncListener, final OnSyncFailureListener onSyncFailureListener) {
        TestSession testSession = TapTimingDatabase.instance(applicationContext).testSessionDao().getById(sessionId);
        syncSession(testSession,onSuccessfulSyncListener,onSyncFailureListener);
    }

    public void syncSession(final TestSession testSession, final OnSuccessfulSyncListener onSuccessfulSyncListener, final OnSyncFailureListener onSyncFailureListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                UserInfo userInfo = TapTimingDatabase.instance(applicationContext).userInfoDao().getById(testSession.getUserId());
                KeyTapCharacteristics[] keyTapCharacteristics = TapTimingDatabase.instance(applicationContext).keyTapCharacteristicsDao().getForSession(testSession.getId());
                FlightTimeCharacteristics[] flightTimeCharacteristics = TapTimingDatabase.instance(applicationContext).flightTimeCharacteristicsDao().getForSession(testSession.getId());
                FirebaseTestSession firebaseTestSession = new FirebaseTestSession(testSession, FirebaseInstanceId.getInstance().getId());
                firebaseTestSession.setUserInfo(userInfo);
                firebaseTestSession.setFlightTimeCharacteristics(Arrays.asList(flightTimeCharacteristics));
                firebaseTestSession.setKeyTapCharacteristics(Arrays.asList(keyTapCharacteristics));
                saveSession(testSession,firebaseTestSession, onSuccessfulSyncListener, onSyncFailureListener);
            }
        });
    }

    private void saveSession(final TestSession testSession, final FirebaseTestSession firebaseTestSession, final OnSuccessfulSyncListener onSuccessfulSyncListener, final OnSyncFailureListener onSyncFailureListener) {
        firestore = FirebaseFirestore.getInstance();
        firestore.collection(FIREBASE_COLLECTION_NAME).
                add(firebaseTestSession).
                addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        markAsSynced(testSession);
                        onSuccessfulSyncListener.onSuccessfulSync();
                        Log.d(TAG, String.format("saved session (id %d), firebase document id: %s",firebaseTestSession.getId(),documentReference.getId()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onSyncFailureListener.onSyncFailure(e);
                        Log.w(TAG, "error saving session session, id" + firebaseTestSession.getId(),e);
                    }
                });
    }

    private void markAsSynced(final TestSession testSession) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                testSession.setSynchronized(true);
                TapTimingDatabase.instance(applicationContext).testSessionDao().update(testSession);
            }
        });
    }

}
