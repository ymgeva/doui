package com.ymgeva.doui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseUser;
import com.ymgeva.doui.login.LoginActivity;
import com.ymgeva.doui.notifications.NotificationsService;
import com.ymgeva.doui.sync.DoUISyncAdapter;
import com.ymgeva.doui.tasks.TaskListActivity;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DoUISyncAdapter.initializeSyncAdapter(getApplicationContext());

        Intent intent = null;
        if (ParseUser.getCurrentUser() == null) {
            intent = new Intent(getApplicationContext(),LoginActivity.class);
        }
        else {
            intent = new Intent(getApplicationContext(),TaskListActivity.class);

            Intent notificationIntent = getIntent();
            String action = notificationIntent.getAction();
            if (NotificationsService.ACTION_SHOW_TASK.equals(action) || NotificationsService.ACTION_URGENT_SHOPPING.equals(action)) {
                long taskId = notificationIntent.getLongExtra(NotificationsService.PARAM_ID,0);
                Intent dismissIntent = new Intent(this,NotificationsService.class);
                dismissIntent.setAction(NotificationsService.ACTION_DISMISS_TASK_NOTIFICATION);
                dismissIntent.putExtra(NotificationsService.PARAM_ID, taskId);
                startService(dismissIntent);
                if (taskId > 0) {
                    intent.setAction(action);
                    intent.putExtra(NotificationsService.PARAM_ID,taskId);
                }
            }
        }
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
