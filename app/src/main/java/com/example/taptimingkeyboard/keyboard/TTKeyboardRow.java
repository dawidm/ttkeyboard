package com.example.taptimingkeyboard.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The row of the {@link TapTimingKeyboard} layout.
 */
public class TTKeyboardRow {

    private ArrayList<TTKeyboardElement> elements = new ArrayList<>();

    /**
     * Add TTKeyboardElement to this row.
     * @param ttKeyboardElement The element.
     */
    public void addElement(TTKeyboardElement ttKeyboardElement) {
        elements.add(ttKeyboardElement);
    }

    /**
     * Get all TTKeyboardElements.
     * @return The elements.
     */
    public List<TTKeyboardElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

}
