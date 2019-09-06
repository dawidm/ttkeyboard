package com.example.taptimingkeyboard.keyboard;

/**
 * An empty space in the {@link TTKeyboardRow} of the {@link TapTimingKeyboard} layout.
 */
public class TTKeyboardSpacer extends TTKeyboardElement {

    //as a proportion of standard button size
    private float size;

    /**
     * @param size See {@link #getSize()}
     */
    public TTKeyboardSpacer(float size) {
        this.size = size;
    }

    /**
     * @return The horizontal size of the spacer. The proportion of the sum of the sizes of all the elements in {@link TTKeyboardRow}
     */
    public float getSize() {
        return size;
    }
}
