package com.ymgeva.doui;

import java.text.SimpleDateFormat;

/**
 * Created by Yoav on 4/20/15.
 */
public class Utility {

    public static String formatShortDate(long date) {
        return new SimpleDateFormat("EE dd/MM").format(date);
    }

    public static String formatTime(long date) {
        return new SimpleDateFormat("HH:mm").format(date);
    }

}
