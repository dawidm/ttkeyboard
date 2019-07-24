package com.example.taptimingkeyboard;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class TapTimingKeyboard {

    public static final String TAG = TapTimingKeyboard.class.getName();

    private View tapTimingKeyboardView;

    public TapTimingKeyboard(Context context, TTKeyboardLayout.Layout layout, TTKeyboardMotionEventListener motionEventListener) {
        Point point = new Point();
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        Log.i(TAG,"screen size: "+point.x+"x"+point.y);
        this.tapTimingKeyboardView = TTKeyboardLayout.withLayout(layout).generateView(context,motionEventListener);
    }

    public View getView() {
        return tapTimingKeyboardView;
    }

}
