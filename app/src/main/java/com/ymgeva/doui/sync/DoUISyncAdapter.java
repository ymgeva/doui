package com.ymgeva.doui.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DoUIContentProvider;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;

import java.net.URI;
import java.util.Date;

/**
 * Created by Yoav on 4/16/15.
 */
public class DoUISyncAdapter extends AbstractThreadedSyncAdapter {

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
            DoUIParseSyncAdapter.getInstance().syncGeneralItems(getContext());
            DoUIParseSyncAdapter.getInstance().syncShoppingItems(getContext());
        }
        else {
            if (table.equals(DoUIContract.PATH_TASKS)) {
                DoUIParseSyncAdapter.getInstance().syncTasks(getContext());
            }
            else if (table.equals(DoUIContract.PATH_GENERAL)) {
                DoUIParseSyncAdapter.getInstance().syncGeneralItems(getContext());
            }
            else if (table.equals(DoUIContract.PATH_SHOPPING)) {
                DoUIParseSyncAdapter.getInstance().syncShoppingItems(getContext());
            }
        }
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

    private static void onAccountCreated(Account newAccount, Context context) {

        DoUISyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context,null);
    }
}
