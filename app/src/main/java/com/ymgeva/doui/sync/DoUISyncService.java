package com.ymgeva.doui.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Yoav on 4/16/15.
 */
public class DoUISyncService extends Service {
    private static final Object mSyncAdapterLock = new Object();
    private static DoUISyncAdapter mSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("TaskmeSyncService", "onCreate - TaskmeSyncService");
        synchronized (mSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new DoUISyncAdapter(getApplicationContext(), true);
            }
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SyncService", "onDestroy");
    }


}
