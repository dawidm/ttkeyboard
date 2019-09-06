package com.example.taptimingkeyboard.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Stores information about the number of errors in a word in a specified test session.
 */
@Entity(tableName = "word_errors", primaryKeys = {"sessionId", "word"})
public class TestSessionWordErrors {

    @NonNull
    private String word;
    private int errors;
    @NonNull
    private long sessionId;

    /**
     * Instantiates a new {@link TestSessionWordErrors}
     *
     * @param word      see {@link #getWord()}
     * @param errors    see {@link #getErrors()}
     * @param sessionId see {@link #getSessionId()}
     */
    public TestSessionWordErrors(String word, int errors, long sessionId) {
        this.word = word;
        this.errors = errors;
        this.sessionId=sessionId;
    }

    /**
     * Gets word.
     *
     * @return One of the words from the test session.
     */
    public String getWord() {
        return word;
    }

    /**
     * Gets errors.
     *
     * @return Number of errors for the word.
     */
    public int getErrors() {
        return errors;
    }

    /**
     * Gets session id.
     *
     * @return The session id
     */
    public long getSessionId() {
        return sessionId;
    }

    /**
     * Generates an array of {@link TestSessionWordErrors} based on map containing numbers of errors for words.
     *
     * @param errorsMap The map of words and numbers of errors.
     * @param sessionId The id of the test session.
     * @return The array of {@link TestSessionWordErrors}.
     */
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
