package com.ymgeva.doui.parse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.notifications.NotificationsService;

/**
 * Created by Yoav on 5/2/15.
 */
public class SyncDoneReceiver extends BroadcastReceiver {
    private String mParseTaskId;
    private int mPushCode;
    private long mLocalId;

    private static final String LOG_TAG = SyncDoneReceiver.class.getSimpleName();

    public SyncDoneReceiver(String parseTaskId, int pushCode, long localId) {
        super();
        this.mParseTaskId = parseTaskId;
        this.mPushCode = pushCode;
        this.mLocalId = localId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(LOG_TAG,"onReceive: Code = "+mPushCode);

        context.unregisterReceiver(this);

        if (mParseTaskId == null && mLocalId > 0) {
            sendPush(context);
        }
        else {
            sendNotification(context);
        }

    }

    private void sendPush(Context context) {

        Cursor cursor = null;

        switch (mPushCode) {
            case DoUIPushBroadcastReceiver.PUSH_CODE_URGENT_TASK: {
                Uri uri = DoUIContract.TaskItemEntry.buildTaskUri(mLocalId);
                cursor = context.getContentResolver().query(uri,new String[] {DoUIContract.TaskItemEntry.COLUMN_PARSE_ID},null,null,null);
                break;
            }
            case DoUIPushBroadcastReceiver.PUSH_CODE_URGENT_SHOPPING: {
                Uri uri = DoUIContract.ShoppingItemEntry.buildShoppingItemUri(mLocalId);
                cursor = context.getContentResolver().query(uri,new String[] {DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID},null,null,null);
                break;
            }
            default: {
                return;
            }
        }

        if (cursor != null && cursor.moveToFirst()) {
            String parseId = cursor.getString(0);
            Log.d(LOG_TAG,"sendingPush Code = "+mPushCode+" parseId = "+parseId);
            DoUIParseSyncAdapter.sendPush(mPushCode,parseId);
        }

    }

    private void sendNotification(Context context) {

        Intent notificationIntent = new Intent(context,NotificationsService.class);
        notificationIntent.putExtra(NotificationsService.PARAM_TASK_PARSE_ID, mParseTaskId);

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
                Log.d(LOG_TAG, "push code does not match");
                return;
            }
        }

        context.startService(notificationIntent);
    }
}
