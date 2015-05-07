package com.ymgeva.doui.shopping;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.sync.DoUISyncAdapter;

public class ShoppingListActivity extends ActionBarActivity implements ShoppingListFragment.ShoppingListFragmentListener {

    private SwipeRefreshLayout mSwipeLayout;
    private ShoppingListReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.shopping_container, new ShoppingListFragment())
                    .commit();
        }
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.shopping_swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DoUISyncAdapter.syncImmediately(getApplicationContext(), DoUIContract.PATH_SHOPPING);
            }
        });

        mReceiver = new ShoppingListReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver,new IntentFilter(R.string.broadcast_sync_done+"."+DoUIContract.PATH_SHOPPING));

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }



    @Override
    public void onDoneClicked(long _id,boolean isDone) {
        ContentValues values = new ContentValues();
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_DONE,isDone);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_IS_DIRTY,true);
        getContentResolver().update(DoUIContract.ShoppingItemEntry.CONTENT_URI,values,"_ID = "+_id,null);
        DoUISyncAdapter.syncImmediately(getApplicationContext(), DoUIContract.PATH_SHOPPING);
    }

    public class ShoppingListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mSwipeLayout.setRefreshing(false);
        }
    }
}
