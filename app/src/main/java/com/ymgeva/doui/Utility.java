package com.ymgeva.doui;

import com.ymgeva.doui.parse.DoUIParseSyncAdapter;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

        String me = DoUIParseSyncAdapter.getUserId();
        String partner = DoUIParseSyncAdapter.getPartnerId();

        if (user.equals(me)) {
            return R.drawable.i_image;
        }

        if (user.equals(partner)){
            return R.drawable.u_image;
        }

        return R.drawable.image_who;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return text;
        }
    }

    public static boolean isDateToday(long date) {
        Calendar nowCal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(date));

        //a task
        return nowCal.get(Calendar.ERA) == cal.get(Calendar.ERA) &&
               nowCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
               nowCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR);
    }


    public static String formatSuccess (int s) {
        return s > 0 ? "Successful" : "Failed";
    }
}
