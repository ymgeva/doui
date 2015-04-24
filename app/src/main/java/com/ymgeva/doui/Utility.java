package com.ymgeva.doui;

import com.ymgeva.doui.parse.DoUIParseSyncAdapter;

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

    public static String formatDateTime(long date) {
        return new SimpleDateFormat("dd/MM HH:mm").format(date);
    }

    public static int imageResourseByUser(String user) {

        if (user == null) {
            return R.drawable.image_who;
        }

        String me = DoUIParseSyncAdapter.getInstance().getUserId();
        String partner = DoUIParseSyncAdapter.getInstance().getPartnerId();

        if (user.equals(me)) {
            return R.drawable.i_image;
        }

        if (user.equals(partner)){
            return R.drawable.u_image;
        }

        return R.drawable.image_who;
    }
}
