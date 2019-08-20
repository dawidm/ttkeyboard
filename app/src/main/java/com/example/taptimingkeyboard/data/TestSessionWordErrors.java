package com.example.taptimingkeyboard.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

@Entity(tableName = "word_errors", primaryKeys = {"sessionId", "word"})
public class TestSessionWordErrors {

    @NonNull
    private String word;
    private int errors;
    @NonNull
    private long sessionId;

    public TestSessionWordErrors(String word, int errors, long sessionId) {
        this.word = word;
        this.errors = errors;
        this.sessionId=sessionId;
    }

    public String getWord() {
        return word;
    }

    public int getErrors() {
        return errors;
    }

    public long getSessionId() {
        return sessionId;
    }

    public static TestSessionWordErrors[] fromMap(Map<String,Integer> errorsMap, long sessionId) {
        Iterator<Map.Entry<String,Integer>> iterator = errorsMap.entrySet().iterator();
        ArrayList<TestSessionWordErrors> errorsArrayList = new ArrayList<>(errorsMap.entrySet().size());
        while (iterator.hasNext()) {
            Map.Entry<String,Integer> currentEntry = iterator.next();
            errorsArrayList.add(new TestSessionWordErrors(currentEntry.getKey(),currentEntry.getValue(),sessionId));
        }
        return errorsArrayList.toArray(new TestSessionWordErrors[errorsArrayList.size()]);
    }

}
