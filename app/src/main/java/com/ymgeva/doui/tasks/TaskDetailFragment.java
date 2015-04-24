package com.ymgeva.doui.tasks;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.ymgeva.doui.R;
import com.ymgeva.doui.Utility;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;

public class TaskDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = TaskDetailFragment.class.getSimpleName();
    public static final String TASK_ID = "task_id";
    private static final int LOADER_TAG = 100;

    private TextView mTitle;
    private TextView mDescription;
    private TextView mDateView;
    private TextView mTimeView;
    private ImageView mCreatedBy;
    private ImageView mAssignedTo;
    private CheckBox mReminder;
    private CheckBox mNotifyWhenDone;
    private TextView mReminderTime;

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



    private long mTaskId;

    public TaskDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mTaskId = savedInstanceState.getLong(TASK_ID);
        }
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(TASK_ID)) {
            getLoaderManager().initLoader(LOADER_TAG, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(TASK_ID)) {
            long newId = arguments.getLong(TASK_ID);
            if (mTaskId != newId) {
                mTaskId = newId;
                getLoaderManager().restartLoader(LOADER_TAG, null, this);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(TASK_ID, mTaskId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.menu_details_fragment, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);

        mTitle = (TextView)rootView.findViewById(R.id.task_detail_title);
        mDescription = (TextView)rootView.findViewById(R.id.task_detail_text);
        mReminderTime = (TextView)rootView.findViewById(R.id.task_detail_reminder_time);
        mCreatedBy = (ImageView)rootView.findViewById(R.id.task_detail_from_image);
        mAssignedTo = (ImageView)rootView.findViewById(R.id.task_detail_to_image);
        mReminder = (CheckBox)rootView.findViewById(R.id.task_detail_reminder_checkbox);
        mNotifyWhenDone = (CheckBox)rootView.findViewById(R.id.task_detail_notify_checkbox);
        mDateView = (TextView)rootView.findViewById(R.id.task_detail_date);
        mTimeView = (TextView)rootView.findViewById(R.id.task_detail_time);

        mTaskId = getArguments().getLong(TASK_ID);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri taskUri = DoUIContract.TaskItemEntry.buildTaskUri(mTaskId);

        return new CursorLoader(
                getActivity(),
                taskUri,
                TASK_COLUMNS,
                null,
                null,
                null
        );    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mTitle.setText(data.getString(COL_TITLE));
            mDescription.setText(data.getString(COL_TEXT));

            String me = DoUIParseSyncAdapter.getInstance().getUserId();
            if (me.equals(data.getString(COL_CREATED_BY))) {
                mCreatedBy.setImageResource(R.drawable.i_image);
            }
            else {
                mCreatedBy.setImageResource(R.drawable.u_image);
            }

            if (me.equals(data.getString(COL_ASSIGNED_TO))) {
                mAssignedTo.setImageResource(R.drawable.i_image);
            }
            else {
                mAssignedTo.setImageResource(R.drawable.u_image);
            }

            mDateView.setText(Utility.formatShortDate(data.getLong(COL_DATE)));
            mTimeView.setText(Utility.formatTime(data.getLong(COL_DATE)));

           mReminder.setChecked(data.getInt(COL_REMINDER) > 0);
           mReminderTime.setText(Utility.formatTime(data.getLong(COL_REMINDER_TIME)));

           mNotifyWhenDone.setChecked(data.getInt(COL_NOTIFY_WHEN_DONE) > 0);



        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
