package com.example.taptimingkeyboard.data;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Stores the lists of words used by test session.
 */
public class WordLists {

    /**
     * Single words list.
     */
    public class WordList {

        private String name;
        private String[] words;

        /**
         * Instantiates a new WordList.
         *
         * @param name see {@link #getName()}
         * @param words see {@link #getWords()}
         */
        public WordList(String name, String[] words) {
            this.name = name;
            this.words = words;
        }

        /**
         * Gets name.
         *
         * @return The name of the list.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets words.
         *
         * @return The words.
         */
        public String[] getWords() {
            return words;
        }

        /**
         * @return Comma separated (no spaces) list of words in this list.
         */
        public String getWordsCsv() {
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0; i<words.length-1;i++) {
                stringBuilder.append(words[i]);
                stringBuilder.append(",");
            }
            stringBuilder.append(words[words.length-1]);
            return stringBuilder.toString();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private ArrayList<WordList> lists;

    /**
     * Instantiates a new WordLists.
     *
     * @param lists see @getLists
     */
    public WordLists(ArrayList<WordList> lists) {
        this.lists = lists;
    }

    /**
     * Gets lists.
     *
     * @return The ArrayList of WordList objects.
     */
    public ArrayList<WordList> getLists() {
        return lists;
    }

    /**
     * Creates instance of WordLists based on remote json file.
     *
     * @param url The url of a json file containing WordLists object.
     * @return The instance of WordLists.
     * @throws IOException Any I/O error when downloading or parsing the json file.
     */
    public static WordLists fromUrl(String url) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(new InputStreamReader(new URL(url).openStream()), WordLists.class);
    }


}
