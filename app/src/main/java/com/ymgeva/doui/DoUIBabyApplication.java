package com.ymgeva.doui;

import android.app.Application;

import com.parse.Parse;
import com.parse.PushService;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;

/**
 * Created by Yoav on 4/12/15.
 */
public class DoUIBabyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

            DoUIParseSyncAdapter.getInstance().init(getApplicationContext());

        }
}
