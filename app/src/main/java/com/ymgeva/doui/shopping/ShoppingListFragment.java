package com.ymgeva.doui.shopping;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DoUIContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();
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
    private ShoppingListFragment mListener;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private ShoppingListAdapter mAdapter;
    private ListView mListView;

    public interface ShoppingListFragmentListener {

    }

    public ShoppingListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
