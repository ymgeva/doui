package com.ymgeva.doui.tasks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.ymgeva.doui.R;
import com.ymgeva.doui.Utility;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;
import com.ymgeva.doui.sync.DoUISyncAdapter;

public class TaskDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = TaskDetailFragment.class.getSimpleName();
    public static final String TASK_ID = "task_id";
    private static final int LOADER_TAG = 100;

    private long mDate;
    private long mReminderTime;
    private long mId;
    private String mCreatedBy;
    private String mAssignedTo;
    private String mParseId;

    private TextView mTitle;
    private TextView mDescription;
    private TextView mDateView;
    private TextView mTimeView;
    private ImageView mCreatedByView;
    private ImageView mAssignedToView;
    private CheckBox mReminder;
    private CheckBox mNotifyWhenDone;
    private TextView mReminderTimeView;

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

    private DetailsFragmentListener mListener;

    public interface DetailsFragmentListener {
        public void onDoneClicked(long _id,boolean isDone);
    }


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
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_edit) {
            openEditActivity();
        }
        else if (id == R.id.action_done) {
            mListener.onDoneClicked(mId,true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);

        mTitle = (TextView)rootView.findViewById(R.id.task_detail_title);
        mDescription = (TextView)rootView.findViewById(R.id.task_detail_text);
        mReminderTimeView = (TextView)rootView.findViewById(R.id.task_detail_reminder_time);
        mCreatedByView = (ImageView)rootView.findViewById(R.id.task_detail_from_image);
        mAssignedToView = (ImageView)rootView.findViewById(R.id.task_detail_to_image);
        mReminder = (CheckBox)rootView.findViewById(R.id.task_detail_reminder_checkbox);
        mNotifyWhenDone = (CheckBox)rootView.findViewById(R.id.task_detail_notify_checkbox);
        mDateView = (TextView)rootView.findViewById(R.id.task_detail_date);
        mTimeView = (TextView)rootView.findViewById(R.id.task_detail_time);

        mTaskId = getArguments().getLong(TASK_ID);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof DetailsFragmentListener)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mListener = (DetailsFragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void openEditActivity() {

        Intent intent = new Intent(getActivity(),EditTaskActivity.class);
        intent.putExtra(EditTaskActivity.IS_NEW_TASK_SETTING,false);
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_TITLE,mTitle.getText().toString());
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_DESCRIPTION,mDescription.getText().toString());
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_REMINDER,mReminder.isChecked());
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE,mNotifyWhenDone.isChecked());
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME,mReminderTime);
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO,mAssignedTo);
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,mCreatedBy);
        intent.putExtra(DoUIContract.TaskItemEntry._ID,mId);
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_PARSE_ID,mParseId);
        intent.putExtra(DoUIContract.TaskItemEntry.COLUMN_DATE,mDate);

        startActivity(intent);

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

            mCreatedBy = data.getString(COL_CREATED_BY);
            mCreatedByView.setImageResource(Utility.imageResourseByUser(mCreatedBy));

            mAssignedTo = data.getString(COL_ASSIGNED_TO);
            mAssignedToView.setImageResource(Utility.imageResourseByUser(mAssignedTo));

            mDate = data.getLong(COL_DATE);
            mDateView.setText(Utility.formatShortDate(mDate));
            mTimeView.setText(Utility.formatTime(mDate));


            mReminder.setChecked(data.getInt(COL_REMINDER) > 0);
            mReminderTime = data.getLong(COL_REMINDER_TIME);
            mReminderTimeView.setText(Utility.formatTime(mReminderTime));

            mNotifyWhenDone.setChecked(data.getInt(COL_NOTIFY_WHEN_DONE) > 0);

            mId = data.getLong(COL_ID);
            mParseId = data.getString(COL_PARSE_ID);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
