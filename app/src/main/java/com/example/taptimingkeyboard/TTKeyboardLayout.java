package com.example.taptimingkeyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TTKeyboardLayout {

    public static final String TAG = TTKeyboardLayout.class.getName();

    public enum Layout {
        SIMPLEST_QWERTY_ASYMMETRIC,
        SIMPLEST_QWERTY_SYMMETRIC,
        SIMPLE_QWERTY_ASYMMETRIC,
        SIMPLE_QWERTY_SYMMETRIC
    }

    private ArrayList<TTKeyboardRow> rows = new ArrayList<>();

    public static TTKeyboardLayout withLayout(Layout layout) {
        switch (layout) {
            case SIMPLEST_QWERTY_ASYMMETRIC:
                return simplestQwertyLayoutAsymmetric();
            case SIMPLEST_QWERTY_SYMMETRIC:
                return simplestQwertyLayoutSymmetric();
            case SIMPLE_QWERTY_ASYMMETRIC:
                return simpleQwertyLayoutAsymmetric();
            case SIMPLE_QWERTY_SYMMETRIC:
                return simpleQwertyLayoutSymmetric();
            default:
                throw new RuntimeException("unknown layout " + layout);
        }
    }

    public List<TTKeyboardRow> getRows() {
        return Collections.unmodifiableList(rows);
    }

    private static TTKeyboardLayout simplestQwertyLayoutAsymmetric() {
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
        secondRow.addElement(new TTKeyboardSpacer(0.25f));
        secondRow.addElement(new TTKeyboardButton('a'));
        secondRow.addElement(new TTKeyboardButton('s'));
        secondRow.addElement(new TTKeyboardButton('d'));
        secondRow.addElement(new TTKeyboardButton('f'));
        secondRow.addElement(new TTKeyboardButton('g'));
        secondRow.addElement(new TTKeyboardButton('h'));
        secondRow.addElement(new TTKeyboardButton('j'));
        secondRow.addElement(new TTKeyboardButton('k'));
        secondRow.addElement(new TTKeyboardButton('l'));
        secondRow.addElement(new TTKeyboardSpacer(0.75f));
        TTKeyboardRow thirdRow = new TTKeyboardRow();
        thirdRow.addElement(new TTKeyboardSpacer(0.75f));
        thirdRow.addElement(new TTKeyboardButton('z'));
        thirdRow.addElement(new TTKeyboardButton('x'));
        thirdRow.addElement(new TTKeyboardButton('c'));
        thirdRow.addElement(new TTKeyboardButton('v'));
        thirdRow.addElement(new TTKeyboardButton('b'));
        thirdRow.addElement(new TTKeyboardButton('n'));
        thirdRow.addElement(new TTKeyboardButton('m'));
        thirdRow.addElement(new TTKeyboardSpacer(2.25f));
        TTKeyboardRow fourthRow = new TTKeyboardRow();
        fourthRow.addElement(new TTKeyboardSpacer(2.75f));
        fourthRow.addElement(new TTKeyboardButton("SPACE",32,5));
        fourthRow.addElement(new TTKeyboardSpacer(2.25f));
        TTKeyboardLayout ttKeyboardLayout = new TTKeyboardLayout();
        ttKeyboardLayout.rows.add(firstRow);
        ttKeyboardLayout.rows.add(secondRow);
        ttKeyboardLayout.rows.add(thirdRow);
        ttKeyboardLayout.rows.add(fourthRow);
        return ttKeyboardLayout;
    }

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
        fourthRow.addElement(new TTKeyboardButton("SPACE",32,5));
        fourthRow.addElement(new TTKeyboardSpacer(2.5f));
        TTKeyboardLayout ttKeyboardLayout = new TTKeyboardLayout();
        ttKeyboardLayout.rows.add(firstRow);
        ttKeyboardLayout.rows.add(secondRow);
        ttKeyboardLayout.rows.add(thirdRow);
        ttKeyboardLayout.rows.add(fourthRow);
        return ttKeyboardLayout;
    }

    private static TTKeyboardLayout simpleQwertyLayoutAsymmetric() {
        //TODO
        throw new RuntimeException("not implemented");
    }

    private static TTKeyboardLayout simpleQwertyLayoutSymmetric() {
        //TODO
        throw new RuntimeException("not implemented");
    }

}
