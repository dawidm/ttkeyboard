package com.example.taptimingkeyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TTKeyboardRow {

    private ArrayList<TTKeyboardElement> elements = new ArrayList<>();

    public void addElement(TTKeyboardElement ttKeyboardElement) {
        elements.add(ttKeyboardElement);
    }

    public List<TTKeyboardElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

}
