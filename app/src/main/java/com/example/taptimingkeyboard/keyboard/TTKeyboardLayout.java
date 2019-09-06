package com.example.taptimingkeyboard.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generates {@link TapTimingKeyboard} layout.
 * The layout contains instances of {@link TTKeyboardRow} and rows contain instances of {@link TTKeyboardElement} - buttons or spacers.
 */
public class TTKeyboardLayout {

    public static final String TAG = TTKeyboardLayout.class.getName();

    public enum Layout {
        SIMPLEST_QWERTY_SYMMETRIC,
        SIMPLE_QWERTY_SYMMETRIC
    }

    private ArrayList<TTKeyboardRow> rows = new ArrayList<>();
    private Double width;

    /**
     * Get an instance of {@link TTKeyboardLayout} with specified buttons layout.
     * @param layout The layout.
     * @return Instance of TTKeyboardLayout
     */
    public static TTKeyboardLayout withLayout(Layout layout) {
        switch (layout) {
            case SIMPLEST_QWERTY_SYMMETRIC:
                return simplestQwertyLayoutSymmetric();
            case SIMPLE_QWERTY_SYMMETRIC:
                return simpleQwertyLayoutSymmetric();
            default:
                throw new RuntimeException("unknown layout " + layout);
        }
    }

    /**
     * Get the rows (containing layout elements) of this layout.
     * @return
     */
    public List<TTKeyboardRow> getRows() {
        return Collections.unmodifiableList(rows);
    }

    /**
     * QWERTY layout with small cap characters and space only. Used by {@link com.example.taptimingkeyboard.activity.TestSessionActivity}.
     * @return TTKeyboardLayout instance.
     */
    private static TTKeyboardLayout simplestQwertyLayoutSymmetric() {
        TTKeyboardRow firstRow = new TTKeyboardRow();
        firstRow.addElement(new TTKeyboardButton('q'));
        firstRow.addElement(new TTKeyboardButton('w'));
        firstRow.addElement(new TTKeyboardButton('e'));
        firstRow.addElement(new TTKeyboardButton('r'));
        firstRow.addElement(new TTKeyboardButton('t'));
        firstRow.addElement(new TTKeyboardButton('y'));
        firstRow.addElement(new TTKeyboardButton('u'));
        firstRow.addElement(new TTKeyboardButton('i'));
        firstRow.addElement(new TTKeyboardButton('o'));
        firstRow.addElement(new TTKeyboardButton('p'));
        TTKeyboardRow secondRow = new TTKeyboardRow();
        secondRow.addElement(new TTKeyboardSpacer(0.5f));
        secondRow.addElement(new TTKeyboardButton('a'));
        secondRow.addElement(new TTKeyboardButton('s'));
        secondRow.addElement(new TTKeyboardButton('d'));
        secondRow.addElement(new TTKeyboardButton('f'));
        secondRow.addElement(new TTKeyboardButton('g'));
        secondRow.addElement(new TTKeyboardButton('h'));
        secondRow.addElement(new TTKeyboardButton('j'));
        secondRow.addElement(new TTKeyboardButton('k'));
        secondRow.addElement(new TTKeyboardButton('l'));
        secondRow.addElement(new TTKeyboardSpacer(0.5f));
        TTKeyboardRow thirdRow = new TTKeyboardRow();
        thirdRow.addElement(new TTKeyboardSpacer(1.5f));
        thirdRow.addElement(new TTKeyboardButton('z'));
        thirdRow.addElement(new TTKeyboardButton('x'));
        thirdRow.addElement(new TTKeyboardButton('c'));
        thirdRow.addElement(new TTKeyboardButton('v'));
        thirdRow.addElement(new TTKeyboardButton('b'));
        thirdRow.addElement(new TTKeyboardButton('n'));
        thirdRow.addElement(new TTKeyboardButton('m'));
        thirdRow.addElement(new TTKeyboardSpacer(1.5f));
        TTKeyboardRow fourthRow = new TTKeyboardRow();
        fourthRow.addElement(new TTKeyboardSpacer(2.5f));
        fourthRow.addElement(new TTKeyboardButton("",32,5));
        fourthRow.addElement(new TTKeyboardSpacer(2.5f));
        TTKeyboardLayout ttKeyboardLayout = new TTKeyboardLayout();
        ttKeyboardLayout.rows.add(firstRow);
        ttKeyboardLayout.rows.add(secondRow);
        ttKeyboardLayout.rows.add(thirdRow);
        ttKeyboardLayout.rows.add(fourthRow);
        return ttKeyboardLayout;
    }

    /**
     * Simple QWERTY layout with small cap characters and space, backspace and return.
     * @return TTKeyboardLayout instance.
     */
    private static TTKeyboardLayout simpleQwertyLayoutSymmetric() {
        TTKeyboardRow firstRow = new TTKeyboardRow();
        firstRow.addElement(new TTKeyboardButton('q'));
        firstRow.addElement(new TTKeyboardButton('w'));
        firstRow.addElement(new TTKeyboardButton('e'));
        firstRow.addElement(new TTKeyboardButton('r'));
        firstRow.addElement(new TTKeyboardButton('t'));
        firstRow.addElement(new TTKeyboardButton('y'));
        firstRow.addElement(new TTKeyboardButton('u'));
        firstRow.addElement(new TTKeyboardButton('i'));
        firstRow.addElement(new TTKeyboardButton('o'));
        firstRow.addElement(new TTKeyboardButton('p'));
        TTKeyboardRow secondRow = new TTKeyboardRow();
        secondRow.addElement(new TTKeyboardSpacer(0.5f));
        secondRow.addElement(new TTKeyboardButton('a'));
        secondRow.addElement(new TTKeyboardButton('s'));
        secondRow.addElement(new TTKeyboardButton('d'));
        secondRow.addElement(new TTKeyboardButton('f'));
        secondRow.addElement(new TTKeyboardButton('g'));
        secondRow.addElement(new TTKeyboardButton('h'));
        secondRow.addElement(new TTKeyboardButton('j'));
        secondRow.addElement(new TTKeyboardButton('k'));
        secondRow.addElement(new TTKeyboardButton('l'));
        secondRow.addElement(new TTKeyboardSpacer(0.5f));
        TTKeyboardRow thirdRow = new TTKeyboardRow();
        thirdRow.addElement(new TTKeyboardSpacer(1.5f));
        thirdRow.addElement(new TTKeyboardButton('z'));
        thirdRow.addElement(new TTKeyboardButton('x'));
        thirdRow.addElement(new TTKeyboardButton('c'));
        thirdRow.addElement(new TTKeyboardButton('v'));
        thirdRow.addElement(new TTKeyboardButton('b'));
        thirdRow.addElement(new TTKeyboardButton('n'));
        thirdRow.addElement(new TTKeyboardButton('m'));
        thirdRow.addElement(new TTKeyboardSpacer(0.5f));
        thirdRow.addElement(new TTKeyboardButton("⌫",8,1));
        TTKeyboardRow fourthRow = new TTKeyboardRow();
        fourthRow.addElement(new TTKeyboardSpacer(2.0f));
        fourthRow.addElement(new TTKeyboardButton("",32,5));
        fourthRow.addElement(new TTKeyboardButton(','));
        fourthRow.addElement(new TTKeyboardButton('.'));
        fourthRow.addElement(new TTKeyboardButton("⏎",13,1));
        TTKeyboardLayout ttKeyboardLayout = new TTKeyboardLayout();
        ttKeyboardLayout.rows.add(firstRow);
        ttKeyboardLayout.rows.add(secondRow);
        ttKeyboardLayout.rows.add(thirdRow);
        ttKeyboardLayout.rows.add(fourthRow);
        return ttKeyboardLayout;
    }

    /**
     * Get the maximum value of the sums of the sizes of elements in each TTKeyboardRow.
     * @return
     */
    public double getWidth() {
        if(width==null)
            calcWidth();
        return width;
    }

    /**
     * See {@link #getWidth()}
     */
    private void calcWidth() {
        double maxRowSize=0;
        Iterator<TTKeyboardRow> it = rows.iterator();
        while (it.hasNext()) {
            Iterator<TTKeyboardElement> itElements = it.next().getElements().iterator();
            double currentRowSize=0;
            while (itElements.hasNext())
                currentRowSize+=itElements.next().getSize();
            if(currentRowSize>maxRowSize)
                maxRowSize=currentRowSize;
        }
        width=maxRowSize;
    }

}
