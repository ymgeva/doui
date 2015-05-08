package com.ymgeva.doui.parse;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.notifications.NotificationsService;
import com.ymgeva.doui.sync.DoUISyncAdapter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yoav on 4/30/15.
 */
public class DoUIPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    private static final String LOG_TAG = DoUIPushBroadcastReceiver.class.getSimpleName();

    public static final String USER_ID = "user_id";
    public static final String PUSH_CODE = "push_code";
    public static final String OBJECT_ID = "object_id";

    public static final int PUSH_CODE_UPDATE_PARTNER = 100;
    public static final int PUSH_CODE_URGENT_TASK = 200;
    public static final int PUSH_CODE_NOTIFY_DONE = 201;
    public static final int PUSH_CODE_URGENT_SHOPPING = 300;



    @Override
    protected void onPushReceive(Context context, Intent intent) {
        String jsonString = intent.getStringExtra("com.parse.Data");
        int pushCode = 0;
        String objectId = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String pushCodeStr = jsonObject.getString(PUSH_CODE);
            pushCode = Integer.parseInt(pushCodeStr);
            objectId = jsonObject.getString(OBJECT_ID);
        } catch (Exception e) {
            Log.d(LOG_TAG,"onPushReceive: JSON string = "+jsonString);

            if (e.getClass().equals(JSONException.class)) {
                Log.d(LOG_TAG,"onPushReceive: json can't be parsed");
            }
            else if (e.getClass().equals(NumberFormatException.class)) {
                Log.d(LOG_TAG,"onPushReceive: push code not properly formatted"+pushCode);
            }
            e.printStackTrace();
            return;
        }

        switch (pushCode) {
            case PUSH_CODE_UPDATE_PARTNER:{
                DoUIParseSyncAdapter.updatePartner(objectId,context.getApplicationContext());
                break;
            }
            case PUSH_CODE_NOTIFY_DONE:
            case PUSH_CODE_URGENT_TASK: {
                SyncDoneReceiver receiver = new SyncDoneReceiver(objectId,pushCode,0);
                context.getApplicationContext().registerReceiver(receiver,new IntentFilter(R.string.broadcast_sync_done+"."+DoUIContract.PATH_TASKS));
                DoUISyncAdapter.syncImmediately(context,DoUIContract.PATH_TASKS);
                break;
            }
            case PUSH_CODE_URGENT_SHOPPING: {
                SyncDoneReceiver receiver = new SyncDoneReceiver(objectId,pushCode,0);
                context.getApplicationContext().registerReceiver(receiver,new IntentFilter(R.string.broadcast_sync_done+"."+DoUIContract.PATH_SHOPPING));
                DoUISyncAdapter.syncImmediately(context,DoUIContract.PATH_SHOPPING);
                break;
            }

            default:{
                Log.d(LOG_TAG,"onPushReceive: unknown push code"+pushCode);
            }
        }
    }
}
