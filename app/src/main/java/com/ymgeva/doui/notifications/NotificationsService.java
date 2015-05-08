package com.ymgeva.doui.notifications;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ymgeva.doui.MainActivity;
import com.ymgeva.doui.R;
import com.ymgeva.doui.Utility;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.sync.DoUISyncAdapter;

import java.util.Date;

public class NotificationsService extends IntentService {

    private static String LOG_TAG = NotificationsService.class.getSimpleName();

    public static final String[] TASK_COLUMNS = {
            DoUIContract.TaskItemEntry._ID,
            DoUIContract.TaskItemEntry.COLUMN_PARSE_ID,
            DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO,
            DoUIContract.TaskItemEntry.COLUMN_DATE,
            DoUIContract.TaskItemEntry.COLUMN_TITLE,
            DoUIContract.TaskItemEntry.COLUMN_DONE,
            DoUIContract.TaskItemEntry.COLUMN_DESCRIPTION,
            DoUIContract.TaskItemEntry.COLUMN_REMINDER,
            DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME,
            DoUIContract.TaskItemEntry.COLUMN_IMAGE,
            DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,
            DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE
    };

    public static final int COL_ID = 0;
    public static final int COL_PARSE_ID = 1;
    public static final int COL_ASSIGNED_TO = 2;
    public static final int COL_DATE = 3;
    public static final int COL_TITLE = 4;
    public static final int COL_DONE = 5;
    public static final int COL_TEXT = 6;
    public static final int COL_REMINDER = 7;
    public static final int COL_REMINDER_TIME = 8;
    public static final int COL_IMAGE = 9;
    public static final int COL_CREATED_BY = 10;
    public static final int COL_NOTIFY_WHEN_DONE = 11;

    public static final String [] SHOPPING_COLUMNS = {
            DoUIContract.ShoppingItemEntry._ID,
            DoUIContract.ShoppingItemEntry.COLUMN_TITLE
    };

    public static final int COL_SHOPPING_ID = 0;
    public static final int COL_SHOPPING_TITLE = 1;



    public static final String ACTION_REMINDER = "com.ymgeva.doui.notifications.action.reminder";
    public static final String ACTION_FIRE_REMINDER_NOTIFICATION = "com.ymgeva.doui.notifications.action.fire_reminder_notification";
    public static final String ACTION_SHOW_TASK = "com.ymgeva.doui.notifications.action.fire_reminder_notification";
    public static final String ACTION_DISMISS_TASK_NOTIFICATION = "com.ymgeva.doui.notifications.action.dismiss_task";
    public static final String ACTION_DISMISS_SHOPPING_NOTIFICATION = "com.ymgeva.doui.notifications.action.dismiss_shopping";
    public static final String ACTION_SNOOZE = "com.ymgeva.doui.notifications.action.snooze";
    public static final String ACTION_DONE = "com.ymgeva.doui.notifications.action.done";
    public static final String ACTION_NOTIFY_TASK_DONE = "com.ymgeva.doui.notifications.action.notify_task_done";
    public static final String ACTION_URGENT_TASK = "com.ymgeva.doui.notifications.action.urgent_task";
    public static final String ACTION_URGENT_SHOPPING = "com.ymgeva.doui.notifications.action.urgent_shopping";
    public static final String ACTION_PARTNER_CONNECTED = "com.ymgeva.doui.notification.action.partner_connected";
    public static final String ACTION_DISMISS_PARTNER_NOTIFICATION = "com.ymgeva.doui.notifications.action.dismiss_shopping";


    public static final String PARAM_ID = "com.ymgeva.doui.notifications.extra.PARAM_ID";
    public static final String PARAM_TASK_PARSE_ID = "task_parse_id";
    public static final String PARAM_PARTNER_NAME = "partner_name";

    private static final int TASKS_TAG_OFFSET = 300000;
    private static final int SHOPPING_TAG_OFFSET = 400000;
    private static final int PARTNER_OFFSET = 500000;


    public static void startWithAction(Context context,String action) {

        Intent intent = new Intent(context, NotificationsService.class);
        intent.setAction(action);
        context.startService(intent);

    }

    public static void cancelTaskNotification(Context context, int taskId) {
        cancelNotificaion(context,TASKS_TAG_OFFSET+taskId);
    }

    public static void cancelShoppingNotification(Context context, int id) {
       cancelNotificaion(context,SHOPPING_TAG_OFFSET+id);
    }

    private static void cancelNotificaion(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public NotificationsService() {
        super("NotificationsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final long taskId = intent.getLongExtra(PARAM_ID,0);
            final String parseId = intent.getStringExtra(PARAM_TASK_PARSE_ID);

            Log.d(LOG_TAG,"onHandleIntent. taskId = "+taskId);

            if (ACTION_REMINDER.equals(action))    {
                setAlarmForReminder(0);
            } else if (ACTION_FIRE_REMINDER_NOTIFICATION.equals(action)) {
                cancelTaskNotification(this, (int) taskId);
                fireReminderNotification(taskId);
            } else if (ACTION_SNOOZE.equals(action)) {
                cancelTaskNotification(this, (int) taskId);
                setAlarmForReminder(taskId);
            } else if (ACTION_DONE.equals(action)) {
                cancelTaskNotification(this, (int) taskId);
                setTaskDone(taskId);
            } else if (ACTION_DISMISS_TASK_NOTIFICATION.equals(action)) {
                dismissReminder(taskId);
                cancelTaskNotification(this, (int) taskId);
            } else if (ACTION_NOTIFY_TASK_DONE.equals(action)) {
                notifyTaskDone(parseId);
            } else if (ACTION_URGENT_TASK.equals(action)) {
                notifyUrgentTask(parseId);
            } else if (ACTION_URGENT_SHOPPING.equals(action)) {
                notifyUrgentShopping(parseId);
            } else if (ACTION_DISMISS_SHOPPING_NOTIFICATION.equals(action)) {
                cancelShoppingNotification(this,(int) taskId);
            } else if (ACTION_PARTNER_CONNECTED.equals(action)) {
                notifyPartnerConnected(intent.getStringExtra(PARAM_PARTNER_NAME));
            }
        }
    }

    private void notifyPartnerConnected(String partnerName) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.doui_white)
                        .setContentTitle(getString(R.string.connected_to_partner,partnerName))
                        .setContentText(getString(R.string.connected_to_partner_text))
                        .setDefaults(Notification.DEFAULT_ALL);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mBuilder.setLargeIcon(largeIcon);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        Intent dismissIntent = new Intent(this, NotificationsService.class);

        dismissIntent.setAction(ACTION_DISMISS_PARTNER_NOTIFICATION);
        PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.addAction(R.drawable.action_notification_dismiss, getString(R.string.dismiss), piDismiss);

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(SHOPPING_TAG_OFFSET, mBuilder.build());
    }

    private void notifyUrgentShopping(String parseId) {

        Cursor cursor = getContentResolver().query(DoUIContract.ShoppingItemEntry.CONTENT_URI,
                SHOPPING_COLUMNS,
                DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID+" = ?",
                new String[]{parseId},null);

        if (cursor != null && cursor.moveToFirst()) {
            long itemId = cursor.getLong(COL_SHOPPING_ID);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.doui_white)
                            .setContentTitle(getString(R.string.urgent_shopping_notification,cursor.getString(COL_SHOPPING_ID)))
                            .setContentText(cursor.getString(COL_SHOPPING_TITLE))
                            .setDefaults(Notification.DEFAULT_ALL);
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            mBuilder.setLargeIcon(largeIcon);

            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setAction(ACTION_URGENT_SHOPPING);
            resultIntent.putExtra(PARAM_ID,itemId);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            Intent dismissIntent = new Intent(this, NotificationsService.class);
            dismissIntent.setAction(ACTION_DISMISS_SHOPPING_NOTIFICATION);
            dismissIntent.putExtra(PARAM_ID,itemId);
            PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(cursor.getString(COL_SHOPPING_TITLE)))
                    .addAction(R.drawable.action_notification_dismiss, getString(R.string.dismiss), piDismiss);

            NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(SHOPPING_TAG_OFFSET +(int)itemId, mBuilder.build());
        }
    }

    private void notifyUrgentTask(String parseId) {

        Cursor cursor = getContentResolver().query(DoUIContract.TaskItemEntry.CONTENT_URI,
                TASK_COLUMNS,
                DoUIContract.TaskItemEntry.COLUMN_PARSE_ID+" = ?",new String[]{parseId},null);

        if (cursor != null && cursor.moveToFirst()) {
            long taskId = cursor.getLong(COL_ID);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.doui_white)
                            .setContentTitle(getString(R.string.urgent_task_notification,cursor.getString(COL_TITLE)))
                            .setContentText(cursor.getString(COL_TITLE))
                            .setDefaults(Notification.DEFAULT_ALL);
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            mBuilder.setLargeIcon(largeIcon);


            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setAction(ACTION_SHOW_TASK);
            resultIntent.putExtra(PARAM_ID,taskId);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            Intent dismissIntent = new Intent(this, NotificationsService.class);
            dismissIntent.setAction(ACTION_DISMISS_TASK_NOTIFICATION);
            dismissIntent.putExtra(PARAM_ID,taskId);
            PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(cursor.getString(COL_TITLE)))
                    .addAction (R.drawable.action_notification_dismiss,getString(R.string.dismiss), piDismiss);

            NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(TASKS_TAG_OFFSET+(int)taskId, mBuilder.build());
        }
    }



    private void notifyTaskDone(String parseId) {
        Cursor cursor = getContentResolver().query(DoUIContract.TaskItemEntry.CONTENT_URI,
                TASK_COLUMNS,
                DoUIContract.TaskItemEntry.COLUMN_PARSE_ID+" = ?",new String[]{parseId},null);
        if (cursor != null && cursor.moveToFirst()) {
            long taskId = cursor.getLong(COL_ID);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.doui_white)
                            .setContentTitle(getString(R.string.task_done_notification,cursor.getString(COL_TITLE)))
                            .setContentText(cursor.getString(COL_TITLE))
                            .setDefaults(Notification.DEFAULT_ALL);
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            mBuilder.setLargeIcon(largeIcon);


            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setAction(ACTION_SHOW_TASK);
            resultIntent.putExtra(PARAM_ID,taskId);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            Intent dismissIntent = new Intent(this, NotificationsService.class);
            dismissIntent.setAction(ACTION_DISMISS_TASK_NOTIFICATION);
            dismissIntent.putExtra(PARAM_ID,taskId);
            PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(cursor.getString(COL_TITLE)))
                    .addAction (R.drawable.action_notification_dismiss,getString(R.string.dismiss), piDismiss);

            NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(TASKS_TAG_OFFSET+(int)taskId, mBuilder.build());
        }
    }

    private void setTaskDone(long taskId) {
        Log.d(LOG_TAG,"setTaskDone. taskId = "+taskId);
        DoUISyncAdapter.setTaskDone(this,taskId,true);
        setAlarmForReminder(0);
    }

    private void setAlarmForReminder(long taskId){
        Log.d(LOG_TAG,"setAlarmForReminder. taskId: "+taskId);

        long reminderTime = 0;

        //case of snooze
        if (taskId > 0) {
            reminderTime = new Date().getTime()+5 * 60 * 1000;
        }

        else { //look for the first task that needs an alarm an set it
            String [] args = {"1","0"};
            Cursor cursor = getContentResolver().query(
                    DoUIContract.TaskItemEntry.CONTENT_URI,
                    TASK_COLUMNS,
                    DoUIContract.TaskItemEntry.COLUMN_REMINDER+" = ? AND "+ DoUIContract.TaskItemEntry.COLUMN_DONE+" = ?",
                    args,
                    DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME+" ASC LIMIT 1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                reminderTime = cursor.getLong(COL_REMINDER_TIME);
                taskId = cursor.getLong(COL_ID);
            }
            else {
                Log.d(LOG_TAG,"No task found that needs alarm. Not setting.");
                return;
            }
        }

        Intent intent = new Intent(this,NotificationsService.class);
        intent.setAction(ACTION_FIRE_REMINDER_NOTIFICATION);
        intent.putExtra(PARAM_ID,taskId);

        Log.d(LOG_TAG,"Setting alarm for "+Utility.formatDateTime(reminderTime)+" taskId: "+taskId);

        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);

    }

    private void fireReminderNotification(long taskId) {

        Log.d(LOG_TAG,"fireNotification. taskId = "+taskId);


        Uri uri = DoUIContract.TaskItemEntry.buildTaskUri(taskId);
        Cursor cursor = getContentResolver().query(uri,TASK_COLUMNS,null,null,null);
        if (cursor != null && cursor.moveToFirst()) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.doui_white)
                            .setContentTitle(cursor.getString(COL_TITLE))
                            .setContentText(cursor.getString(COL_TITLE))
                            .setDefaults(Notification.DEFAULT_ALL);
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            mBuilder.setLargeIcon(largeIcon);


            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setAction(ACTION_SHOW_TASK);
            resultIntent.putExtra(PARAM_ID,taskId);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            Intent dismissIntent = new Intent(this, NotificationsService.class);
            dismissIntent.setAction(ACTION_DISMISS_TASK_NOTIFICATION);
            dismissIntent.putExtra(PARAM_ID,taskId);
            PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent snoozeIntent = new Intent(this, NotificationsService.class);
            snoozeIntent.setAction(ACTION_SNOOZE);
            snoozeIntent.putExtra(PARAM_ID,taskId);
            PendingIntent piSnooze = PendingIntent.getService(this, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent doneIntent = new Intent(this, NotificationsService.class);
            doneIntent.setAction(ACTION_DONE);
            doneIntent.putExtra(PARAM_ID,taskId);
            PendingIntent piDone = PendingIntent.getService(this, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(cursor.getString(COL_TITLE)))
                    .addAction (R.drawable.action_notification_dismiss,getString(R.string.dismiss), piDismiss)
                    .addAction (R.drawable.action_notification_snooze,getString(R.string.snooze), piSnooze)
                    .addAction(R.drawable.action_notification_done,getString(R.string.done),piDone);


            NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(TASKS_TAG_OFFSET+(int)taskId, mBuilder.build());
        }
    }

    private void dismissReminder(long taskId) {
        Log.d(LOG_TAG, "setTaskDone. taskId = " + taskId);

        if (taskId == 0) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DoUIContract.TaskItemEntry.COLUMN_REMINDER, false);
        values.put(DoUIContract.TaskItemEntry.COLUMN_IS_DIRTY, true);

        int updated = getContentResolver().update(DoUIContract.TaskItemEntry.CONTENT_URI,values,"_ID = "+taskId,null);
        Log.d(LOG_TAG,"dismissReminder "+ Utility.formatSuccess(updated));

        setAlarmForReminder(0);
    }

}
