package com.ymgeva.doui.parse;



import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Created by Yoav on 5/1/15.
 */
public class ParseErrorHandler {

    private static final String LOG_TAG = ParseErrorHandler.class.getSimpleName();

    public static void handleParseException(ParseException e) {

        switch (e.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN: {
                ParseUser.logOut();
            }
            default: {
                return;
            }
        }
    }
}
