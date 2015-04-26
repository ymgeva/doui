package com.ymgeva.doui.tasks;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.sync.DoUISyncAdapter;

public class TaskListActivity extends ActionBarActivity
        implements TaskListFragment.Callbacks, TaskDetailFragment.DetailsFragmentListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SwipeRefreshLayout mSwipeLayout;
    private TaskListReceiver mReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        if (findViewById(R.id.task_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((TaskListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.task_list))
                    .setActivateOnItemClick(true);
        }

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DoUISyncAdapter.syncImmediately(getApplicationContext(), DoUIContract.PATH_TASKS);
            }
        });

        mReciever = new TaskListReceiver();
        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_task_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent intent = new Intent(this,EditTaskActivity.class);
            intent.putExtra(EditTaskActivity.IS_NEW_TASK_SETTING,true);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(long id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(TaskDetailFragment.TASK_ID, id);
            TaskDetailFragment fragment = new TaskDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.task_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, TaskDetailActivity.class);
            detailIntent.putExtra(TaskDetailFragment.TASK_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReciever,new IntentFilter(R.string.broadcast_sync_done+"."+DoUIContract.PATH_TASKS));

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReciever);
    }

    @Override
    public void onDoneClicked(long _id) {
        taskDone(_id);
    }

    private void taskDone(long _id) {
        ContentValues values = new ContentValues();
        values.put(DoUIContract.TaskItemEntry.COLUMN_DONE,true);
        values.put(DoUIContract.TaskItemEntry.COLUMN_IS_DIRTY,true);
        getContentResolver().update(DoUIContract.TaskItemEntry.CONTENT_URI,values,"_ID = "+_id,null);
        DoUISyncAdapter.syncImmediately(getApplicationContext(), DoUIContract.TaskItemEntry.TABLE_NAME);
    }

    public class TaskListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mSwipeLayout.setRefreshing(false);
        }
    }
}
