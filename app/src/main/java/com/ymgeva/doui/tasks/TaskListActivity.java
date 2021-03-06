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

import com.ymgeva.doui.MainActivity;
import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.login.LoginActivity;
import com.ymgeva.doui.notifications.NotificationsService;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;
import com.ymgeva.doui.shopping.ShoppingListActivity;
import com.ymgeva.doui.sync.DoUISyncAdapter;

public class TaskListActivity extends ActionBarActivity
        implements TaskListFragment.Callbacks, TaskDetailFragment.DetailsFragmentListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SwipeRefreshLayout mSwipeLayout;
    private TaskListReceiver mReceiver;

    public static final String EXTRA_IN_SYNC = "com.ymgeva.doui.tasklist.action.in_sync";

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

        mReceiver = new TaskListReceiver();

        Intent intent = getIntent();
        if (NotificationsService.ACTION_SHOW_TASK.equals(intent.getAction())) {
            onItemSelected(intent.getLongExtra(NotificationsService.PARAM_ID,0));
        }
        else if (NotificationsService.ACTION_URGENT_SHOPPING.equals(intent.getAction())) {
            Intent shoppingIntent = new Intent(this,ShoppingListActivity.class);
            startActivity(shoppingIntent);
            overridePendingTransition(R.anim.fadein,R.anim.fadeout);
        } else if(intent.getBooleanExtra(EXTRA_IN_SYNC,false)) {

            mSwipeLayout.post(new Runnable() {
                @Override public void run() {
                    mSwipeLayout.setRefreshing(true);
                }
            });
        }

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
        if (id == R.id.action_shopping) {
            Intent intent = new Intent(this,ShoppingListActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fadein,R.anim.fadeout);
            return true;
        }
        if (id == R.id.action_logout) {
            DoUIParseSyncAdapter.logout(getApplicationContext());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.action_connect_to_partner) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(LoginActivity.LOGIN_MODE,LoginActivity.LOGIN_MODE_CONNECT_TO_PARTNER);
            overridePendingTransition(R.anim.fadein,R.anim.fadeout);
            startActivity(intent);
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
        registerReceiver(mReceiver,new IntentFilter(R.string.broadcast_sync_done+"."+DoUIContract.PATH_TASKS));

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onDoneClicked(long _id,boolean isDone) {
        taskDone(_id,isDone);
    }

    private void taskDone(long _id,boolean isDone) {
        DoUISyncAdapter.setTaskDone(this,_id,isDone);
    }

    public class TaskListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mSwipeLayout.setRefreshing(false);
        }
    }
}
