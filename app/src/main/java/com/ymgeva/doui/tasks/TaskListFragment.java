package com.ymgeva.doui.tasks;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import com.ymgeva.doui.R;
import com.ymgeva.doui.SwipeGestureListener;
import com.ymgeva.doui.data.DoUIContract;

/**
 * A list fragment representing a list of Tasks. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TaskDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TaskListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = TaskListFragment.class.getSimpleName();
    private static final int TASK_LIST_LOADER = 0;
    private static final String SELECTED_KEY = "selected_position";



    public static final String[] TASK_COLUMNS = {
            DoUIContract.TaskItemEntry._ID,
            DoUIContract.TaskItemEntry.COLUMN_PARSE_ID,
            DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO,
            DoUIContract.TaskItemEntry.COLUMN_DATE,
            DoUIContract.TaskItemEntry.COLUMN_TITLE,
            DoUIContract.TaskItemEntry.COLUMN_DONE
    };

    public static final int COL_ID = 0;
    public static final int COL_PARSE_ID = 1;
    public static final int COL_ASSIGNED_TO = 2;
    public static final int COL_DATE = 3;
    public static final int COL_TITLE = 4;
    public static final int COL_DONE = 5;


    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private Callbacks mCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private TaskListAdapter mAdapter;
    private ListView mListView;

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(long id);
        public void onDoneClicked(long _id,boolean isDone);
    }

    public TaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mAdapter = new TaskListAdapter(getActivity(),null,0);

        View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);

        mListView = (ListView)rootView.findViewById(R.id.listview_tasks);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callbacks)getActivity())
                            .onItemSelected(cursor.getLong(COL_ID));
                    mActivatedPosition = position;
                }

            }
        });
        mListView.setOnTouchListener(new SwipeGestureListener(getActivity()) {
            @Override
            public void swipeRight(int x,int y) {
                swipeDone(x,y,true);
            }
            @Override
            public void swipeLeft(int x,int y) {
                swipeDone(x,y,false);
            }

        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mActivatedPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(TASK_LIST_LOADER, null, this);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    private void swipeDone(int x,int y,boolean isDone) {
        int position = mListView.pointToPosition(x,y);
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position) && mCallbacks != null) {
            mCallbacks.onDoneClicked(cursor.getLong(COL_ID),isDone);
            mActivatedPosition = position;
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        mListView.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mListView.setItemChecked(mActivatedPosition, false);
        } else {
            mListView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                DoUIContract.TaskItemEntry.CONTENT_URI,
                TASK_COLUMNS,
                null,
                null,
                DoUIContract.TaskItemEntry.COLUMN_DATE+" ASC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG,"onLoadFinished with "+data.getCount()+" rows");
        mAdapter.swapCursor(data);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mActivatedPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
