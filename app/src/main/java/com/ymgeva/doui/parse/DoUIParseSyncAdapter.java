package com.ymgeva.doui.parse;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.parse.items.GeneralItem;
import com.ymgeva.doui.parse.items.ShoppingItem;
import com.ymgeva.doui.parse.items.TaskItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Yoav on 4/16/15.
 */
public class DoUIParseSyncAdapter {

    private final String LOG_TAG = DoUIParseSyncAdapter.class.getSimpleName();

    private static DoUIParseSyncAdapter instance;
    public static DoUIParseSyncAdapter getInstance() {
        if (instance == null) {
            instance = new DoUIParseSyncAdapter();
        }
        return instance;
    }

    private DoUIParseSyncAdapter() {
    }

    public void init(Context context) {

        ParseObject.registerSubclass(TaskItem.class);
        ParseObject.registerSubclass(GeneralItem.class);
        ParseObject.registerSubclass(ShoppingItem.class);

        Parse.initialize(context, "FcgtxMp25GF3Y9NPOTm7CcOAABAkhTdNmTHnPL1X", "BJcZI0Bxor39HE1GrQDo7suJhjlOXImSMINYBDyC");

    }

    public String getUserId() {
        return ParseUser.getCurrentUser().getObjectId();
    }

    public String getPartnerId() {
        return ParseUser.getCurrentUser().getString("partner_id");
    }

    public boolean canSyncToParse(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni == null) || !(ni.isConnected())) {
            Log.e(LOG_TAG,"No network access");
            return false;
        }
        if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            Log.e(LOG_TAG,"User is not logged");
            return false;
        }
        return true;
    }

    public void syncTasks(final Context context) {

        String me = ParseUser.getCurrentUser().getObjectId();

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        Date today = cal.getTime();

        ParseQuery<TaskItem> queryCreatedBy = TaskItem.getQuery();
        queryCreatedBy.whereEqualTo(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,me);

        ParseQuery<TaskItem> queryAssignedTo = TaskItem.getQuery();
        queryAssignedTo.whereEqualTo(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,me);


        List<ParseQuery<TaskItem>> queries = new ArrayList<>();
        queries.add(queryCreatedBy);
        queries.add(queryAssignedTo);

        ParseQuery<TaskItem> query = ParseQuery.or(queries);
        query.whereGreaterThanOrEqualTo(DoUIContract.TaskItemEntry.COLUMN_DATE,today);
        query.whereNotEqualTo(DoUIContract.TaskItemEntry.COLUMN_DONE,true);

        query.findInBackground(new FindCallback<TaskItem>() {
            @Override
            public void done(List<TaskItem> taskItems, ParseException e) {
                List<ContentValues> allValues = new ArrayList<ContentValues>();
                if (e == null) {
                    for (final TaskItem taskItem : taskItems) {
                        ContentValues values = new ContentValues();
                        values.put(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY, taskItem.getCreatedBy());
                        values.put(DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO, taskItem.getAssignedTo());
                        values.put(DoUIContract.TaskItemEntry.COLUMN_DONE,taskItem.getDone());
                        values.put(DoUIContract.TaskItemEntry.COLUMN_TITLE,taskItem.getTitle());
                        try {
                            values.put(DoUIContract.TaskItemEntry.COLUMN_IMAGE,taskItem.getImage().getData());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        values.put(DoUIContract.TaskItemEntry.COLUMN_DATE,taskItem.getDate().getTime());
                        values.put(DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME,taskItem.getReminderTime().getTime());
                        values.put(DoUIContract.TaskItemEntry.COLUMN_REMINDER,taskItem.getReminder());
                        values.put(DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE,taskItem.getNotifyWhenDone());
                        values.put(DoUIContract.TaskItemEntry.COLUMN_DESCRIPTION,taskItem.getItemDescription());
                        values.put(DoUIContract.TaskItemEntry.COLUMN_PARSE_ID,taskItem.getObjectId());

                        allValues.add(values);

                    }

                    int rows = context.getContentResolver().bulkInsert(DoUIContract.TaskItemEntry.CONTENT_URI,
                            allValues.toArray(new ContentValues[allValues.size()]));
                    Log.v(LOG_TAG,"Entered "+rows+" rows");
                    notifyOnDone(context,DoUIContract.PATH_TASKS);

                }
            }
        });

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
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_DONE,item.getDone());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_NOTIFY_WHEN_DONE,item.getNotifyWhenDone());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_TITLE, item.getTitle());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_PARSE_ID,item.getObjectId());
                        values.put(DoUIContract.GeneralItemEntry.COLUMN_URGENT,item.getUrgent());

                        allValues.add(values);

                    }

                    int rows = context.getContentResolver().bulkInsert(DoUIContract.GeneralItemEntry.CONTENT_URI,
                            allValues.toArray(new ContentValues[allValues.size()]));
                    Log.v(LOG_TAG,"Entered "+rows+" rows");
                    notifyOnDone(context,DoUIContract.PATH_GENERAL);

                }
            }
        });
    }

    public void syncShoppingItems(final Context context) {

        String me = ParseUser.getCurrentUser().getObjectId();
        String partner = ParseUser.getCurrentUser().getString("partner_id");

        ParseQuery<ShoppingItem> queryCreatedByMe = ShoppingItem.getQuery();
        queryCreatedByMe.whereEqualTo(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY,me);

        ParseQuery<ShoppingItem> queryCreatedByPartner = ShoppingItem.getQuery();
        queryCreatedByPartner.whereEqualTo(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY,partner);


        List<ParseQuery<ShoppingItem>> queries = new ArrayList<>();
        queries.add(queryCreatedByMe);
        queries.add(queryCreatedByPartner);

        ParseQuery<ShoppingItem> query = ParseQuery.or(queries);
        query.whereNotEqualTo(DoUIContract.ShoppingItemEntry.COLUMN_DONE,true);

        query.findInBackground(new FindCallback<ShoppingItem>() {
            @Override
            public void done(List<ShoppingItem> items, ParseException e) {
                List<ContentValues> allValues = new ArrayList<ContentValues>();
                if (e == null) {
                    for (final ShoppingItem item : items) {
                        ContentValues values = new ContentValues();
                        values.put(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY, item.getCreatedBy());
                        values.put(DoUIContract.ShoppingItemEntry.COLUMN_DONE,item.getDone());
                        values.put(DoUIContract.ShoppingItemEntry.COLUMN_TITLE,item.getTitle());
                        values.put(DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID,item.getObjectId());
                        values.put(DoUIContract.ShoppingItemEntry.COLUMN_URGENT,item.getUrgent());
                        values.put(DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY,item.getQuantity());


                        allValues.add(values);

                    }

                    int rows = context.getContentResolver().bulkInsert(DoUIContract.ShoppingItemEntry.CONTENT_URI,
                            allValues.toArray(new ContentValues[allValues.size()]));
                    Log.v(LOG_TAG,"Entered "+rows+" rows");
                    notifyOnDone(context,DoUIContract.PATH_SHOPPING);
                }
            }
        });
    }

    private void notifyOnDone(Context context,String filter) {
       Intent intent = new Intent();
       intent.setAction(R.string.broadcast_sync_done+"."+filter);
       context.sendBroadcast(intent);

    }
}
