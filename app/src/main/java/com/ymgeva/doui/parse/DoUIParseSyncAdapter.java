package com.ymgeva.doui.parse;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.ymgeva.doui.MainActivity;
import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DbHelper;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.notifications.NotificationsService;
import com.ymgeva.doui.parse.items.GeneralItem;
import com.ymgeva.doui.parse.items.ShoppingItem;
import com.ymgeva.doui.parse.items.TaskItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Yoav on 4/16/15.
 */
public class DoUIParseSyncAdapter {

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

    public static final int COL_TASKS_ID = 0;
    public static final int COL_TASKS_PARSE_ID = 1;
    public static final int COL_TASKS_ASSIGNED_TO = 2;
    public static final int COL_TASKS_DATE = 3;
    public static final int COL_TASKS_TITLE = 4;
    public static final int COL_TASKS_DONE = 5;
    public static final int COL_TASKS_TEXT = 6;
    public static final int COL_TASKS_REMINDER = 7;
    public static final int COL_TASKS_REMINDER_TIME = 8;
    public static final int COL_TASKS_IMAGE = 9;
    public static final int COL_TASKS_CREATED_BY = 10;
    public static final int COL_TASKS_NOTIFY_WHEN_DONE = 11;

    public static final String[] SHOPPING_COLUMNS = {
            DoUIContract.ShoppingItemEntry._ID,
            DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID,
            DoUIContract.ShoppingItemEntry.COLUMN_TITLE,
            DoUIContract.ShoppingItemEntry.COLUMN_DONE,
            DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY,
            DoUIContract.ShoppingItemEntry.COLUMN_URGENT,
            DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY,
    };

    public static final int COL_SHOPPING_ID = 0;
    public static final int COL_SHOPPING_PARSE_ID = 1;
    public static final int COL_SHOPPING_TITLE = 2;
    public static final int COL_SHOPPING_DONE = 3;
    public static final int COL_SHOPPING_QUANTITY = 4;
    public static final int COL_SHOPPING_URGENT = 5;
    public static final int COL_SHOPPING_CREATED_BY =6;



    private static final String LOG_TAG = DoUIParseSyncAdapter.class.getSimpleName();

    public static final String USER_ID = "user_id";
    public static final String PARTNER_ID = "partner_id";

    private static DoUIParseSyncAdapter instance;
    public static DoUIParseSyncAdapter getInstance() {
        if (instance == null) {
            instance = new DoUIParseSyncAdapter();
        }
        return instance;
    }

    private Map<String,Integer> mSyncDoneActions;

    private DoUIParseSyncAdapter() {
    }

    public void init(final Context context) {

        ParseObject.registerSubclass(TaskItem.class);
        ParseObject.registerSubclass(GeneralItem.class);
        ParseObject.registerSubclass(ShoppingItem.class);

        Parse.initialize(context, "FcgtxMp25GF3Y9NPOTm7CcOAABAkhTdNmTHnPL1X", "BJcZI0Bxor39HE1GrQDo7suJhjlOXImSMINYBDyC");
        ParseUser.enableRevocableSessionInBackground();
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    ParseErrorHandler.handleParseException(e, context);
                }
            }
        });

    }

    public static String getUserId() {
        return ParseUser.getCurrentUser().getObjectId();
    }

    public static String getPartnerId() {
        return ParseUser.getCurrentUser().getString(PARTNER_ID);
    }

    public boolean canSyncToParse(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni == null) || !(ni.isConnected())) {
            Log.e(LOG_TAG,"No network access");
            return false;
        }
//        try {
//            Thread.sleep(3000,0);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            ParseErrorHandler.handleParseException(new ParseException(ParseException.INVALID_SESSION_TOKEN,"User is not logged"),context);
            return false;
        }
        if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            ParseErrorHandler.handleParseException(new ParseException(ParseException.INVALID_SESSION_TOKEN, "User is not logged"), context);
            return false;
        }
        return true;
    }

    public void syncTasks(final Context context) {

        if (!canSyncToParse(context)) {
            Log.e(LOG_TAG,"Sync is not possible");
            notifyOnDone(context,DoUIContract.PATH_TASKS);
        }

        Cursor cursor = context.getContentResolver().query(DoUIContract.TaskItemEntry.CONTENT_URI, TASK_COLUMNS, DoUIContract.TaskItemEntry.COLUMN_IS_DIRTY + " = 1", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.v(LOG_TAG,"Has unsaved items, will upload first");
            uploadTasks(context, cursor);
        }
        else {
            downloadTasks(context);
        }
    }

    private void uploadTasks(final Context context,Cursor cursor) {
        final List<TaskItem> taskItems = new ArrayList<>();
        do {
            TaskItem taskItem = new TaskItem();
            String parseId = cursor.getString(COL_TASKS_PARSE_ID);
            if (!parseId.equals(DoUIContract.NOT_SYNCED)) {
                taskItem.setObjectId(parseId);
            }
            taskItem.setAssignedTo(cursor.getString(COL_TASKS_ASSIGNED_TO));
            taskItem.setCreatedBY(cursor.getString(COL_TASKS_CREATED_BY));
            taskItem.setDate(new Date(cursor.getLong(COL_TASKS_DATE)));
            taskItem.setDone(cursor.getInt(COL_TASKS_DONE) > 0);
            taskItem.setItemDescription(cursor.getString(COL_TASKS_TEXT));
            taskItem.setNotifyWhenDone(cursor.getInt(COL_TASKS_NOTIFY_WHEN_DONE) > 0);
            taskItem.setReminder(cursor.getInt(COL_TASKS_REMINDER) > 0);
            taskItem.setReminderTime(new Date(cursor.getLong(COL_TASKS_REMINDER_TIME)));
            taskItem.setTitle(cursor.getString(COL_TASKS_TITLE));
            taskItem.put("LOCAL_ID",cursor.getLong(COL_TASKS_ID));

            taskItems.add(taskItem);
        } while (cursor.moveToNext());

        try {
            ParseObject.saveAll(taskItems);
            Log.v(LOG_TAG, taskItems.size() + " saved to Parse");
            for (TaskItem taskItem : taskItems) {
                ContentValues values = new ContentValues();
                values.put(DoUIContract.TaskItemEntry.COLUMN_PARSE_ID, taskItem.getObjectId());
                values.put(DoUIContract.TaskItemEntry.COLUMN_IS_DIRTY, false);
                context.getContentResolver().update(DoUIContract.TaskItemEntry.CONTENT_URI,
                        values, "_ID = " + taskItem.getLong("LOCAL_ID"),
                        null);
            }
            downloadTasks(context);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Failed to save, will not download.");
            notifyOnDone(context,DoUIContract.PATH_TASKS);
            ParseErrorHandler.handleParseException(e,context);

        }
    }

    private void downloadTasks(final Context context) {
        String me = ParseUser.getCurrentUser().getObjectId();
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        Date today = cal.getTime();

        ParseQuery<TaskItem> queryCreatedBy = TaskItem.getQuery();
        queryCreatedBy.whereEqualTo(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,me);

        ParseQuery<TaskItem> queryAssignedTo = TaskItem.getQuery();
        queryAssignedTo.whereEqualTo(DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO,me);


        List<ParseQuery<TaskItem>> queries = new ArrayList<>();
        queries.add(queryCreatedBy);
        queries.add(queryAssignedTo);

        ParseQuery<TaskItem> query = ParseQuery.or(queries);
        query.whereGreaterThanOrEqualTo(DoUIContract.TaskItemEntry.COLUMN_DATE, today);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long lastUpdate = prefs.getLong(DoUIContract.TaskItemEntry.LAST_SYNC_TIME,0);
        if (lastUpdate > 0) {
            query.whereGreaterThanOrEqualTo("updatedAt",new Date(lastUpdate));
        }

        List<TaskItem> taskItems = null;
        try {
            taskItems = query.find();
            List<ContentValues> newValues = new ArrayList<ContentValues>();
            int updated = 0;
            for (final TaskItem taskItem : taskItems) {
                ContentValues values = new ContentValues();
                values.put(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY, taskItem.getCreatedBy());
                values.put(DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO, taskItem.getAssignedTo());
                values.put(DoUIContract.TaskItemEntry.COLUMN_DONE,taskItem.getDone());
                values.put(DoUIContract.TaskItemEntry.COLUMN_TITLE,taskItem.getTitle());
//                        try {
//                            values.put(DoUIContract.TaskItemEntry.COLUMN_IMAGE,taskItem.getImage().getData());
//                        } catch (Exception e1) {
//                            e1.printStackTrace();
//                        }
                values.put(DoUIContract.TaskItemEntry.COLUMN_DATE,taskItem.getDate().getTime());
                values.put(DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME,taskItem.getReminderTime().getTime());
                values.put(DoUIContract.TaskItemEntry.COLUMN_REMINDER,taskItem.getReminder());
                values.put(DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE,taskItem.getNotifyWhenDone());
                values.put(DoUIContract.TaskItemEntry.COLUMN_DESCRIPTION,taskItem.getItemDescription());
                values.put(DoUIContract.TaskItemEntry.COLUMN_PARSE_ID,taskItem.getObjectId());
                values.put(DoUIContract.TaskItemEntry.COLUMN_IS_DIRTY,false);

                Cursor cursor = context.getContentResolver().query(DoUIContract.TaskItemEntry.CONTENT_URI,
                        TASK_COLUMNS,
                        DoUIContract.TaskItemEntry.COLUMN_PARSE_ID+" = ?",
                        new String[]{taskItem.getObjectId()},null);
                if (cursor != null && cursor.moveToFirst()) {
                    updated += context.getContentResolver().update(DoUIContract.TaskItemEntry.CONTENT_URI,
                            values,
                            DoUIContract.TaskItemEntry.COLUMN_PARSE_ID+" = ?",
                            new String[]{taskItem.getObjectId()});

                }
                else {
                    newValues.add(values);
                }
            }
            Log.v(LOG_TAG, "Updated " + updated + " rows");

            int rows = context.getContentResolver().bulkInsert(DoUIContract.TaskItemEntry.CONTENT_URI,
                    newValues.toArray(new ContentValues[newValues.size()]));
            Log.v(LOG_TAG, "Entered " + rows + " rows");



            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putLong(DoUIContract.TaskItemEntry.LAST_SYNC_TIME,new Date().getTime());
            editor.commit();

        } catch (ParseException e) {
            ParseErrorHandler.handleParseException(e, context);
        }

        notifyOnDone(context,DoUIContract.PATH_TASKS);
        NotificationsService.startWithAction(context,NotificationsService.ACTION_REMINDER);

    }

    public void syncGeneralItems(final Context context) {

        String me = ParseUser.getCurrentUser().getObjectId();


        ParseQuery<GeneralItem> queryCreatedBy = GeneralItem.getQuery();
        queryCreatedBy.whereEqualTo(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,me);

        ParseQuery<GeneralItem> queryAssignedTo = GeneralItem.getQuery();
        queryAssignedTo.whereEqualTo(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,me);


        List<ParseQuery<GeneralItem>> queries = new ArrayList<>();
        queries.add(queryCreatedBy);
        queries.add(queryAssignedTo);

        ParseQuery<GeneralItem> query = ParseQuery.or(queries);
        query.whereNotEqualTo(DoUIContract.GeneralItemEntry.COLUMN_DONE,true);

        query.findInBackground(new FindCallback<GeneralItem>() {
            @Override
            public void done(List<GeneralItem> items, ParseException e) {
                List<ContentValues> allValues = new ArrayList<ContentValues>();
                if (e == null) {
                    for (final GeneralItem item : items) {
                        ContentValues values = new ContentValues();
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_CREATED_BY, item.getCreatedBy());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_ASSIGNED_TO, item.getAssignedTo());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_DONE, item.getDone());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_NOTIFY_WHEN_DONE, item.getNotifyWhenDone());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_TITLE, item.getTitle());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_PARSE_ID, item.getObjectId());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_URGENT, item.getUrgent());

                        allValues.add(values);

                    }

                    int rows = context.getContentResolver().bulkInsert(DoUIContract.GeneralItemEntry.CONTENT_URI,
                            allValues.toArray(new ContentValues[allValues.size()]));
                    Log.v(LOG_TAG, "Entered " + rows + " rows");
                    notifyOnDone(context, DoUIContract.PATH_GENERAL);

                }
            }
        });
    }

    public void syncShoppingItems(final Context context) {

        if (!canSyncToParse(context)) {
            Log.e(LOG_TAG,"Sync is not possible");
            notifyOnDone(context,DoUIContract.PATH_SHOPPING);
        }

        Cursor cursor = context.getContentResolver().query(DoUIContract.ShoppingItemEntry.CONTENT_URI, SHOPPING_COLUMNS, DoUIContract.ShoppingItemEntry.COLUMN_IS_DIRTY + " = 1", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.v(LOG_TAG,"Has unsaved shopping items, will upload first");
            uploadShoppingItems(context, cursor);
        }
        else {
            downloadShoppingItems(context);
        }

    }

    private void uploadShoppingItems(final Context context, Cursor cursor) {
        final List<ShoppingItem> shoppingItems = new ArrayList<>();
        do {
            ShoppingItem shoppingItem = new ShoppingItem();
            String parseId = cursor.getString(COL_SHOPPING_PARSE_ID);
            if (!parseId.equals(DoUIContract.NOT_SYNCED)) {
                shoppingItem.setObjectId(parseId);
            }
            shoppingItem.setCreatedBY(cursor.getString(COL_SHOPPING_CREATED_BY));
            shoppingItem.setDone(cursor.getInt(COL_SHOPPING_DONE) > 0);
            shoppingItem.setUrgent(cursor.getInt(COL_SHOPPING_URGENT) > 0);
            shoppingItem.setQuantity(cursor.getInt(COL_SHOPPING_QUANTITY));
            shoppingItem.setTitle(cursor.getString(COL_SHOPPING_TITLE));
            shoppingItem.put("LOCAL_ID",cursor.getLong(COL_SHOPPING_ID));

            shoppingItems.add(shoppingItem);
        } while (cursor.moveToNext());

        try {
            ParseObject.saveAll(shoppingItems);
            Log.v(LOG_TAG, shoppingItems.size() + " saved to Parse");
            for (ShoppingItem shoppingItem : shoppingItems) {
                ContentValues values = new ContentValues();
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID, shoppingItem.getObjectId());
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_IS_DIRTY, false);
                context.getContentResolver().update(DoUIContract.ShoppingItemEntry.CONTENT_URI,
                        values, "_ID = " + shoppingItem.getLong("LOCAL_ID"),
                        null);
            }
            downloadShoppingItems(context);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Failed to save shopping items, will not download.", e);
            notifyOnDone(context,DoUIContract.PATH_SHOPPING);
            ParseErrorHandler.handleParseException(e, context);
        }
    }

    private void downloadShoppingItems(final Context context) {

        String me = ParseUser.getCurrentUser().getObjectId();
        String partner = ParseUser.getCurrentUser().getString(PARTNER_ID);

        ParseQuery<ShoppingItem> queryCreatedByMe = ShoppingItem.getQuery();
        queryCreatedByMe.whereEqualTo(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY,me);

        ParseQuery<ShoppingItem> queryCreatedByPartner = ShoppingItem.getQuery();
        queryCreatedByPartner.whereEqualTo(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY,partner);


        List<ParseQuery<ShoppingItem>> queries = new ArrayList<>();
        queries.add(queryCreatedByMe);
        queries.add(queryCreatedByPartner);

        ParseQuery<ShoppingItem> query = ParseQuery.or(queries);
        //query.whereNotEqualTo(DoUIContract.ShoppingItemEntry.COLUMN_DONE,true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Date lastUpdate = new Date(prefs.getLong(DoUIContract.ShoppingItemEntry.LAST_SYNC_TIME,0));
        query.whereGreaterThanOrEqualTo("updatedAt",lastUpdate);

        List<ShoppingItem> items = null;
        try {
            items = query.find();
            List<ContentValues> newValues = new ArrayList<ContentValues>();
            int updated = 0;
            for (final ShoppingItem item : items) {
                ContentValues values = new ContentValues();
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY, item.getCreatedBy());
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_DONE,item.getDone());
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_TITLE,item.getTitle());
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID,item.getObjectId());
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_URGENT,item.getUrgent());
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY,item.getQuantity());
                values.put(DoUIContract.ShoppingItemEntry.COLUMN_LAST_UPDATE,item.getUpdatedAt().getTime());

                Cursor cursor = context.getContentResolver().query(DoUIContract.ShoppingItemEntry.CONTENT_URI,
                        SHOPPING_COLUMNS,
                        DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID+" = ?",
                        new String[]{item.getObjectId()},null);
                if (cursor != null && cursor.moveToFirst()) {
                    updated += context.getContentResolver().update(DoUIContract.ShoppingItemEntry.CONTENT_URI,
                            values,
                            DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID+" = ?",
                            new String[]{item.getObjectId()});

                }
                else {
                    newValues.add(values);
                }

                Log.v(LOG_TAG,"downloadShoppingItems: Updated "+updated+" rows");
                int rows = context.getContentResolver().bulkInsert(DoUIContract.ShoppingItemEntry.CONTENT_URI,
                        newValues.toArray(new ContentValues[newValues.size()]));
                Log.v(LOG_TAG,"downloadShoppingItems: Entered "+rows+" rows");

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putLong(DoUIContract.ShoppingItemEntry.LAST_SYNC_TIME,new Date().getTime());
                editor.commit();
            }
        }
         catch (ParseException e) {
            ParseErrorHandler.handleParseException(e,context);
        }
        notifyOnDone(context,DoUIContract.PATH_SHOPPING);
    }

    private void notifyOnDone(Context context,String filter) {
       Intent intent = new Intent();
       intent.setAction(R.string.broadcast_sync_done+"."+filter);
       context.sendBroadcast(intent);
    }

    public static void updateInstallation() {

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        ParseUser currentUser = ParseUser.getCurrentUser();
        installation.put(DoUIParseSyncAdapter.USER_ID,currentUser.getObjectId());
        installation.saveInBackground();
    }

    public static void updatePartner(String partnerId) {

        final ParseUser me = ParseUser.getCurrentUser();
        me.put(PARTNER_ID, partnerId);
        me.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
                else {

                }
            }
        });
    }

    public static void sendPush(int pushCode,String objectId) {
        sendPush(pushCode,getPartnerId(),objectId);
    }

    public static void sendPush(int pushCode,String userId, String objectId) {

        HashMap<String,Object> map = new HashMap<>();
        map.put(DoUIPushBroadcastReceiver.USER_ID,userId);
        map.put(DoUIPushBroadcastReceiver.OBJECT_ID,objectId);
        map.put(DoUIPushBroadcastReceiver.PUSH_CODE,Integer.toString(pushCode));
        ParseCloud.callFunctionInBackground("SendPushCode", map, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void logout(final Context context) {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
                DbHelper helper = new DbHelper(context);
                helper.onUpgrade(helper.getWritableDatabase(), 0, 0);
            }
        });

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(DoUIContract.ShoppingItemEntry.LAST_SYNC_TIME);
        editor.remove(DoUIContract.TaskItemEntry.LAST_SYNC_TIME);
        editor.commit();


    }

}
