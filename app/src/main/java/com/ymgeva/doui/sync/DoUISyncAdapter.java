package com.ymgeva.doui.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ymgeva.doui.R;
import com.ymgeva.doui.Utility;
import com.ymgeva.doui.data.DoUIContentProvider;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;
import com.ymgeva.doui.parse.DoUIPushBroadcastReceiver;
import com.ymgeva.doui.parse.items.TaskItem;

import java.net.URI;
import java.util.Date;

/**
 * Created by Yoav on 4/16/15.
 */
public class DoUISyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = DoUISyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public DoUISyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        String table = bundle.getString("requestedTable");

        if (table == null) {
            DoUIParseSyncAdapter.getInstance().syncTasks(getContext());
            //DoUIParseSyncAdapter.getInstance().syncGeneralItems(getContext());
            DoUIParseSyncAdapter.getInstance().syncShoppingItems(getContext());
        }
        else {
            if (table.equals(DoUIContract.PATH_TASKS)) {
                DoUIParseSyncAdapter.getInstance().syncTasks(getContext());
            }
//            else if (table.equals(DoUIContract.PATH_GENERAL)) {
//                DoUIParseSyncAdapter.getInstance().syncGeneralItems(getContext());
//            }
            else if (table.equals(DoUIContract.PATH_SHOPPING)) {
                DoUIParseSyncAdapter.getInstance().syncShoppingItems(getContext());
            }
        }
        deleteOldRecords();
    }

    public void deleteOldRecords() {

        long today = Utility.getTodayMs();

        //delete tasks that are before today
        int deleted = getContext().getContentResolver().delete(DoUIContract.TaskItemEntry.CONTENT_URI,
                DoUIContract.TaskItemEntry.COLUMN_DATE+" < ?",
                new String[] {Long.toString(today)});
        Log.d(LOG_TAG,"Deleted "+deleted+" rows from tasks table");

        //delete done shopping items that were closed before today
        deleted = getContext().getContentResolver().delete(DoUIContract.ShoppingItemEntry.CONTENT_URI,
                DoUIContract.ShoppingItemEntry.COLUMN_LAST_UPDATE+" < "+today+" AND "+ DoUIContract.ShoppingItemEntry.COLUMN_DONE+" = 1",
                null);
        Log.d(LOG_TAG,"Deleted "+deleted+" rows from shopping table");

    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void syncImmediately(Context context,String table) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putString("requestedTable",table);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void removeSyncAccount(Context context) {

        ContentResolver.cancelSync(getSyncAccount(context),context.getString(R.string.content_authority));
        ContentResolver.removePeriodicSync(getSyncAccount(context),context.getString(R.string.content_authority),new Bundle());

    }

    public static void onAccountCreated(Context context) {

        DoUISyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(getSyncAccount(context), context.getString(R.string.content_authority), true);
        syncImmediately(context,null);
    }

    public static void setTaskDone(Context context,long taskId,boolean isDone) {

        ContentValues values = new ContentValues();
        values.put(DoUIContract.TaskItemEntry.COLUMN_DONE,isDone);
        values.put(DoUIContract.TaskItemEntry.COLUMN_IS_DIRTY,true);

        int updated = context.getContentResolver().update(DoUIContract.TaskItemEntry.CONTENT_URI,values,"_ID = "+taskId,null);
        if (updated > 0) {
            DoUISyncAdapter.syncImmediately(context, DoUIContract.PATH_TASKS);
        }
        Log.d(LOG_TAG, "setTaskDone " + Utility.formatSuccess(updated));

        Uri uri = DoUIContract.TaskItemEntry.buildTaskUri(taskId);
        final String [] projection = {
                DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE,
                DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO,
                DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,
                DoUIContract.TaskItemEntry.COLUMN_PARSE_ID,
        };
        Cursor cursor = context.getContentResolver().query(uri,projection,null,null,null);
        if (cursor != null && cursor.moveToFirst() &&
                cursor.getInt(0) > 0 &&
                DoUIParseSyncAdapter.getUserId().equals(cursor.getString(1)) &&
                DoUIParseSyncAdapter.getPartnerId().equals(cursor.getString(2))) {

            DoUIParseSyncAdapter.sendPush(DoUIPushBroadcastReceiver.PUSH_CODE_NOTIFY_DONE,cursor.getString(3));
        }
    }
}
