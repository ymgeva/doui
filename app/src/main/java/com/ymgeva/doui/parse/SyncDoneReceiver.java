package com.ymgeva.doui.parse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.notifications.NotificationsService;

/**
 * Created by Yoav on 5/2/15.
 */
public class SyncDoneReceiver extends BroadcastReceiver {
    private String mParseTaskId;
    private int mPushCode;
    private long mLocalId;

    public SyncDoneReceiver(String parseTaskId, int pushCode, long localId) {
        super();
        this.mParseTaskId = parseTaskId;
        this.mPushCode = pushCode;
        this.mLocalId = localId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        context.unregisterReceiver(this);

        if (mParseTaskId == null && mLocalId > 0) {
            sendPush(context);
        }
        else {
            sendNotification(context);
        }

    }

    private void sendPush(Context context) {

        Uri uri = DoUIContract.TaskItemEntry.buildTaskUri(mLocalId);
        Cursor cursor = context.getContentResolver().query(uri,new String[] {DoUIContract.TaskItemEntry.COLUMN_PARSE_ID},null,null,null);
        if (cursor != null && cursor.moveToFirst()) {
            DoUIParseSyncAdapter.sendPush(mPushCode,cursor.getString(0));
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
                //Log.d(LOG_TAG, "push code does not match");
                return;
            }
        }

        context.startService(notificationIntent);
    }
}
