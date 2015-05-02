package com.ymgeva.doui.parse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ymgeva.doui.notifications.NotificationsService;

/**
 * Created by Yoav on 5/2/15.
 */
public class SyncDoneReceiver extends BroadcastReceiver {
    private String mTaskId;
    private int mPushCode;

    public SyncDoneReceiver(String mTaskId, int mPushCode) {
        super();
        this.mTaskId = mTaskId;
        this.mPushCode = mPushCode;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        context.unregisterReceiver(this);

        Intent notificationIntent = new Intent(context,NotificationsService.class);
        notificationIntent.putExtra(NotificationsService.PARAM_TASK_PARSE_ID,mTaskId);

        switch (mPushCode) {
            case DoUIPushBroadcastReceiver.PUSH_CODE_NOTIFY_DONE: {
                notificationIntent.setAction(NotificationsService.ACTION_NOTIFY_TASK_DONE);
                break;
            }
            case DoUIPushBroadcastReceiver.PUSH_CODE_URGENT_SHOPPING: {
                notificationIntent.setAction(NotificationsService.ACTION_URGENT_SHOPPING);
                break;
            }
            case DoUIPushBroadcastReceiver.PUSH_CODE_URGENT_TASK: {
                notificationIntent.setAction(NotificationsService.ACTION_URGENT_TASK);
                break;
            }
            default: {
                //Log.d(LOG_TAG, "push code does not match");
                return;
            }
        }

        context.startService(notificationIntent);
    }
}
