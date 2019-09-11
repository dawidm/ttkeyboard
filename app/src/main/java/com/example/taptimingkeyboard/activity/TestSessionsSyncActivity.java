package com.example.taptimingkeyboard.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.data.TapTimingDatabase;
import com.example.taptimingkeyboard.data.TestSession;
import com.example.taptimingkeyboard.data.UserInfo;
import com.example.taptimingkeyboard.data.firebase.FirebaseSessionSync;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TestSessionsSyncActivity extends AppCompatActivity {

    private ListView testSessionsListView;
    private Button syncButton;
    private List<TestSessionUserName> testSessionUserNameList;
    private FirebaseSessionSync firebaseSessionSync;

    private class TestSessionUserName {

        private TestSession testSession;
        private String userName;

        public TestSessionUserName(TestSession testSession, String userName) {
            this.testSession = testSession;
            this.userName = userName;
        }

        public TestSession getSessionId() {
            return testSession;
        }

        public String getUserName() {
            return userName;
        }

        @Override
        public String toString() {
            return String.format("%s, session %d",userName,testSession.getId());
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_sessions_sync);
        firebaseSessionSync = new FirebaseSessionSync(getApplicationContext());
        testSessionsListView=findViewById(R.id.sessions_list_view);
        syncButton=findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync();
            }
        });
        populateList();
    }

    private void sync() {
        syncButton.setEnabled(false);
        final Set<TestSessionUserName> toSync = Collections.synchronizedSet(new HashSet<TestSessionUserName>());
        toSync.addAll(testSessionUserNameList);
        final List<TestSessionUserName> failedSessions = Collections.synchronizedList(new LinkedList<TestSessionUserName>());
        Iterator<TestSessionUserName> iterator = testSessionUserNameList.iterator();
        while (iterator.hasNext()) {
            final TestSessionUserName testSessionUserName = iterator.next();
            firebaseSessionSync.syncSession(testSessionUserName.testSession.getId(), new FirebaseSessionSync.OnSuccessfulSyncListener() {
                @Override
                public void onSuccessfulSync() {
                    toSync.remove(testSessionUserName);
                    if(toSync.size()==0) {
                        testSessionUserNameList=failedSessions;
                        updateLayoutAfterSync();
                    }
                }
            }, new FirebaseSessionSync.OnSyncFailureListener() {
                @Override
                public void onSyncFailure(Exception e) {
                    toSync.remove(testSessionUserName);
                    failedSessions.add(testSessionUserName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TestSessionsSyncActivity.this, "Error sending " + testSessionUserName.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    if(toSync.size()==0) {
                        updateLayoutAfterSync();
                    }
                }
            });
        }
    }

    private void populateList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TestSession[] testSessions = TapTimingDatabase.instance(getApplicationContext()).testSessionDao().getAll();
                testSessionUserNameList = new LinkedList<>();
                for(int i=0;i<testSessions.length;i++) {
                    if(!testSessions[i].isSynchronized()) {
                        UserInfo userInfo=TapTimingDatabase.instance(getApplicationContext()).userInfoDao().getById(testSessions[i].getUserId());
                        testSessionUserNameList.add(new TestSessionUserName(testSessions[i],userInfo.toString()));
                    }
                }
                updateListViewOnUiThread();
            }
        });
    }

    private void updateLayoutAfterSync() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateListViewOnUiThread();
            }
        });
    }

    private void updateListViewOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(testSessionUserNameList.isEmpty()) {
                    syncButton.setEnabled(false);
                    testSessionsListView.setAdapter(new ArrayAdapter<>(TestSessionsSyncActivity.this,android.R.layout.simple_list_item_1, Arrays.asList(new String[] {getString(R.string.info_no_unsent_results)})));
                } else
                    testSessionsListView.setAdapter(new ArrayAdapter<>(TestSessionsSyncActivity.this,android.R.layout.simple_list_item_1,testSessionUserNameList));
            }
        });
    }
}
