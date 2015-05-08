package com.ymgeva.doui.shopping;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ymgeva.doui.R;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;
import com.ymgeva.doui.parse.DoUIPushBroadcastReceiver;
import com.ymgeva.doui.parse.SyncDoneReceiver;
import com.ymgeva.doui.sync.DoUISyncAdapter;
import com.ymgeva.doui.uiobjects.CheckableImageView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();
    private static final int SHOPPING_LIST_LOADER = 101;
    private static final String SELECTED_KEY = "selected_position";


    public static final String[] SHOPPING_PROJECTION = {
            DoUIContract.ShoppingItemEntry._ID,
            DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID,
            DoUIContract.ShoppingItemEntry.COLUMN_TITLE,
            DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY,
            DoUIContract.ShoppingItemEntry.COLUMN_DONE,
            DoUIContract.ShoppingItemEntry.COLUMN_URGENT
    };

    public static final int COL_ID = 0;
    public static final int COL_PARSE_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_QUANTITY = 3;
    public static final int COL_DONE = 4;
    public static final int COL_URGENT = 5;


    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private ShoppingListFragmentListener mListener;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private ShoppingListAdapter mAdapter;
    private ListView mListView;
    private LinearLayout mNewItemLayout;
    private EditText mNewItemText;
    private CheckableImageView mNewItemUrgent;

    private ActionMode mActionMode;
    private Menu mMenu;
    private long mEditId;


    public interface ShoppingListFragmentListener {
        public void onDoneClicked(long _id,boolean isDone);
    }

    public ShoppingListFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        mAdapter = new ShoppingListAdapter(getActivity(),null,0);
        mListView = (ListView) rootView.findViewById(R.id.listview_shopping);
        mListView.setAdapter(mAdapter);
//        mListView.setOnTouchListener(new SwipeGestureListener(getActivity()) {
//            @Override
//            public void swipeRight(int x,int y) {
//                swipeDone(x,y,false);
//            }
//            @Override
//            public void swipeLeft(int x,int y) {
//                swipeDone(x,y,true);
//            }
//
//        });
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mActivatedPosition == i) {
                    mActivatedPosition = ListView.INVALID_POSITION;
                    cancelEdit();
                    mListView.clearChoices();
                    mListView.requestLayout();
                    showUnselectedMenu();
                }
                else {
                    mActivatedPosition = i;
                    if (mNewItemLayout.getVisibility() == View.VISIBLE) {
                        editItem();
                    }
                    else {
                        showSelectedMenu();
                    }
                }
                setActivatedPosition(mActivatedPosition);
            }
        });

        mNewItemLayout = (LinearLayout) rootView.findViewById(R.id.layout_new_shopping);

        mNewItemText = (EditText) rootView.findViewById(R.id.new_shopping_title);
        mNewItemUrgent = (CheckableImageView) rootView.findViewById(R.id.new_shopping_urgent_checkbox);
        mNewItemUrgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.performClick();
            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_shopping_list, menu);
        mMenu = menu;
        showUnselectedMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            addNewItem();
        }
        else if (id == R.id.action_edit) {
            editItem();
        }
        else if (id == R.id.action_save) {
            saveNewItem();
        }
        else if (id == R.id.action_done) {
            doneClicked(true);
        }
        else if (id == R.id.action_cancel) {
            cancelEdit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            mActivatedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
            setActivatedPosition(mActivatedPosition);

        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getLoaderManager().initLoader(SHOPPING_LIST_LOADER, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof ShoppingListFragmentListener)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mListener = (ShoppingListFragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.shopping_context_menu,menu);
    }

    private void swipeDone(int x,int y,boolean isDone) {
        int position = mListView.pointToPosition(x,y);
        mActivatedPosition = position;
        doneClicked(isDone);

    }

    private void doneClicked(boolean isDone) {
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(mActivatedPosition) && mListener != null) {
            mListener.onDoneClicked(cursor.getLong(COL_ID),isDone);
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mListView.setItemChecked(mActivatedPosition, false);
        } else {
            mListView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void showEditItemMenu() {

        mMenu.findItem(R.id.action_edit).setVisible(false);
        mMenu.findItem(R.id.action_add).setVisible(false);
        mMenu.findItem(R.id.action_done).setVisible(false);

        mMenu.findItem(R.id.action_save).setVisible(true);
        mMenu.findItem(R.id.action_cancel).setVisible(true);

    }

    private void showSelectedMenu() {
        mMenu.findItem(R.id.action_edit).setVisible(true);
        mMenu.findItem(R.id.action_add).setVisible(true);
        mMenu.findItem(R.id.action_done).setVisible(true);

        mMenu.findItem(R.id.action_save).setVisible(false);
        mMenu.findItem(R.id.action_cancel).setVisible(false);
    }

    private void showUnselectedMenu() {
        mMenu.findItem(R.id.action_edit).setVisible(false);
        mMenu.findItem(R.id.action_add).setVisible(true);
        mMenu.findItem(R.id.action_done).setVisible(false);

        mMenu.findItem(R.id.action_save).setVisible(false);
        mMenu.findItem(R.id.action_cancel).setVisible(false);
    }

    private void showNewItemLayout(boolean show){

        mNewItemLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            mNewItemText.setText("");
            mNewItemUrgent.setChecked(false);
        }
        if (mActivatedPosition == ListView.INVALID_POSITION) {
            showUnselectedMenu();
        }
        else {
            showSelectedMenu();
        }
    }

    private void addNewItem() {
        mEditId = 0;
        showNewItemLayout(true);
        showEditItemMenu();
    }

    private void editItem() {

        showNewItemLayout(true);
        showEditItemMenu();

        final Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(mActivatedPosition)) {
            mNewItemText.setText(cursor.getString(COL_TITLE));
            mNewItemUrgent.setChecked(cursor.getInt(COL_URGENT) > 0);
            mEditId = cursor.getLong(COL_ID);
        }

    }

    private void cancelEdit() {
        showNewItemLayout(false);
    }

    private void saveNewItem() {
        String title = mNewItemText.getText().toString();
        if (title == null || title.length() == 0) {
            Toast.makeText(getActivity(),"Item title can't be empty",Toast.LENGTH_LONG).show();
            return;
        }

        boolean isUrgent = mNewItemUrgent.isChecked();

        ContentValues values = new ContentValues();
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_TITLE,title);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY,0);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_URGENT,isUrgent);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY, DoUIParseSyncAdapter.getUserId());
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_DONE,false);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_IS_DIRTY,true);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID,DoUIContract.NOT_SYNCED);

        if (mEditId > 0) {
            getActivity().getContentResolver().update(DoUIContract.ShoppingItemEntry.CONTENT_URI,
                    values,
                    DoUIContract.ShoppingItemEntry._ID+" = "+ mEditId,
                    null);
        }
        else {
            Uri newRow = getActivity().getContentResolver().insert(DoUIContract.ShoppingItemEntry.CONTENT_URI,values);
            mEditId = ContentUris.parseId(newRow);
        }
        if (isUrgent) {
            SyncDoneReceiver receiver = new SyncDoneReceiver(null,DoUIPushBroadcastReceiver.PUSH_CODE_URGENT_SHOPPING, mEditId);
            getActivity().getApplicationContext().registerReceiver(receiver,new IntentFilter(R.string.broadcast_sync_done+"."+DoUIContract.PATH_SHOPPING));
        }
        DoUISyncAdapter.syncImmediately(getActivity().getApplicationContext(),DoUIContract.PATH_SHOPPING);

        showNewItemLayout(false);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                DoUIContract.ShoppingItemEntry.CONTENT_URI,
                SHOPPING_PROJECTION,
                null,
                null,
                DoUIContract.ShoppingItemEntry.COLUMN_DONE+", "+ DoUIContract.ShoppingItemEntry.COLUMN_URGENT+" DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished with " + data.getCount() + " rows");
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
//

}
