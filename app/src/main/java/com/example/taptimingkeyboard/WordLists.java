package com.example.taptimingkeyboard;

import java.util.ArrayList;

public class WordLists {
    public class WordList {
        private String name;
        private String wordsCsv;
        public WordList(String name, String wordsCsv) {
            this.name = name;
            this.wordsCsv = wordsCsv;
        }
        public String getName() {
            return name;
        }
        public String getWordsCsv() {
            return wordsCsv;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    private ArrayList<WordList> lists;
    public WordLists(ArrayList<WordList> lists) {
        this.lists = lists;
    }
    public ArrayList<WordList> getLists() {
        return lists;
    }
}
