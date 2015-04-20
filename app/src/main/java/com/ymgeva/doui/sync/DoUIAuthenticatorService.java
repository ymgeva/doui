package com.ymgeva.doui.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Yoav on 4/16/15.
 */
public class DoUIAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private DoUIAuthenticator mDoUIAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mDoUIAuthenticator = new DoUIAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mDoUIAuthenticator.getIBinder();
    }
}
