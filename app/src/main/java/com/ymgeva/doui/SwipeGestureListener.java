package com.ymgeva.doui;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Yoav on 4/25/15.
 */
public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener implements
        View.OnTouchListener {
    Context context;
    GestureDetector gDetector;
    static final int SWIPE_MIN_DISTANCE = 120;
    static final int SWIPE_MAX_OFF_PATH = 250;
    static final int SWIPE_THRESHOLD_VELOCITY = 200;

    public SwipeGestureListener() {
        super();
    }

    public SwipeGestureListener(Context context) {
        this(context, null);
    }

    public SwipeGestureListener(Context context, GestureDetector gDetector) {

        if (gDetector == null)
            gDetector = new GestureDetector(context, this);

        this.context = context;
        this.gDetector = gDetector;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH
                    || Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY) {
                return false;
            }
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                swipeUp((int)e1.getX(),(int)e1.getY());
            } else if (e2.getY() - e1.getY() >SWIPE_MIN_DISTANCE) {
                swipeDown((int)e1.getX(),(int)e1.getY());

            }
        } else {
            if (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
                return false;
            }
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                swipeLeft((int)e1.getX(),(int)e1.getY());
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                swipeRight((int)e1.getX(),(int)e1.getY());
            }
        }

        return super.onFling(e1, e2, velocityX, velocityY);

    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {



        return gDetector.onTouchEvent(event);
    }

    public GestureDetector getDetector() {
        return gDetector;
    }

    public void swipeLeft(int x,int y){};
    public void swipeRight(int x,int y){};
    public void swipeUp(int x,int y){};
    public void swipeDown(int x,int y){};

}
