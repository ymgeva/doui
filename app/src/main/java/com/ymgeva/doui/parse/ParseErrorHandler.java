package com.ymgeva.doui.parse;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.ymgeva.doui.MainActivity;
import com.ymgeva.doui.R;

/**
 * Created by Yoav on 5/1/15.
 */
public class ParseErrorHandler {

    private static final String LOG_TAG = ParseErrorHandler.class.getSimpleName();

    public static void handleParseException(ParseException e, final Context context) {

        switch (e.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN: {
                Log.e(LOG_TAG,"FOOBAR");
//                DoUIParseSyncAdapter.logout(context);
//                new AlertDialog.Builder(context.getApplicationContext()).
//                        setMessage(R.string.please_relogin).
//                        setCancelable(false).
//                        setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
//                                context.startActivity(intent);
//                            }
//                        }).show();
            }
            default: {
                e.printStackTrace();
            }
        }
    }
}
