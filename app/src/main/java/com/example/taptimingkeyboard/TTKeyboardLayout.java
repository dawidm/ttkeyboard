package com.example.taptimingkeyboard;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.ArrayList;
import java.util.Iterator;

public class TTKeyboardLayout {

    public static final String TAG = TTKeyboardLayout.class.getName();

    private ArrayList<TTKeyboardRow> rows = new ArrayList<>();

    public static TTKeyboardLayout qwertyLayout() {
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
        fourthRow.addElement(new TTKeyboardButton("SPACE",032,5));
        fourthRow.addElement(new TTKeyboardSpacer(2.25f));
        TTKeyboardLayout ttKeyboardLayout = new TTKeyboardLayout();
        ttKeyboardLayout.rows.add(firstRow);
        ttKeyboardLayout.rows.add(secondRow);
        ttKeyboardLayout.rows.add(thirdRow);
        ttKeyboardLayout.rows.add(fourthRow);
        return ttKeyboardLayout;
    }

    public View generateView(Context context, final TTKeyboardMotionEventListener ttMotionEventListener) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        for(Iterator<TTKeyboardRow> itRow = rows.iterator();itRow.hasNext();) {
            TTKeyboardRow row = itRow.next();
            LinearLayout rowLinearLayout = new LinearLayout(context);
            for(Iterator<TTKeyboardElement> itElement = row.getElements().iterator(); itElement.hasNext();) {
                TTKeyboardElement element = itElement.next();
                if(element instanceof TTKeyboardButton) {
                    final TTKeyboardButton ttButton = (TTKeyboardButton)element;
                    Button button = new Button(context);
                    button.setText(ttButton.getLabel());
                    button.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,ttButton.getSize()));
                    button.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            ttMotionEventListener.onMotionEvent(ttButton,motionEvent);
                            Log.v(TAG,ttButton.getLabel()+" onTouch called: "+motionEvent.getAction());
                            return false;
                        }
                    });
                    Log.v(TAG,"adding button " + ttButton.getLabel() + " size " +ttButton.getSize());
                    rowLinearLayout.addView(button);
                }
                if(element instanceof TTKeyboardSpacer) {
                    TTKeyboardSpacer ttSpacer = (TTKeyboardSpacer)element;
                    Space space = new Space(context);
                    space.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,ttSpacer.getSize()));
                    Log.v(TAG,"adding space size " + ttSpacer.getSize());
                    rowLinearLayout.addView(space);
                }
            }
            mainLayout.addView(rowLinearLayout);
        }
        return mainLayout;
    }

}
