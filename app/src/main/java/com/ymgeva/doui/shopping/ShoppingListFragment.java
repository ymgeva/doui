package com.ymgeva.doui.shopping;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ymgeva.doui.R;
import com.ymgeva.doui.SwipeGestureListener;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;
import com.ymgeva.doui.parse.DoUIPushBroadcastReceiver;
import com.ymgeva.doui.parse.SyncDoneReceiver;
import com.ymgeva.doui.sync.DoUISyncAdapter;

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
    private ImageButton mNewItemButton;
    private LinearLayout mNewItemLayout;
    private EditText mNewItemQuantity;
    private EditText mNewItemText;
    private CheckBox mNewItemUrgent;
    private ImageButton mNewItemSave;

    public interface ShoppingListFragmentListener {
        public void onDoneClicked(long _id,boolean isDone);
    }

    public ShoppingListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        mAdapter = new ShoppingListAdapter(getActivity(),null,0);
        mListView = (ListView) rootView.findViewById(R.id.listview_shopping);
        mListView.setAdapter(mAdapter);
        mListView.setOnTouchListener(new SwipeGestureListener(getActivity()) {
            @Override
            public void swipeRight(int x,int y) {
                swipeDone(x,y,false);
            }
            @Override
            public void swipeLeft(int x,int y) {
                swipeDone(x,y,true);
            }

        });

        mNewItemButton = (ImageButton) rootView.findViewById(R.id.image_button_add_shopping);
        mNewItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNewItemButton.setVisibility(View.GONE);
                mNewItemLayout.setVisibility(View.VISIBLE);
            }
        });
        mNewItemLayout = (LinearLayout) rootView.findViewById(R.id.layout_new_shopping);
        mNewItemLayout.setVisibility(View.GONE);

        mNewItemQuantity = (EditText) rootView.findViewById(R.id.new_shopping_quantity);
        mNewItemText = (EditText) rootView.findViewById(R.id.new_shopping_title);
        mNewItemUrgent = (CheckBox) rootView.findViewById(R.id.new_shopping_urgent_checkbox);
        mNewItemSave = (ImageButton) rootView.findViewById(R.id.new_shopping_save);
        mNewItemSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewItem();
            }
        });

        return rootView;
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

    private void swipeDone(int x,int y,boolean isDone) {
        int position = mListView.pointToPosition(x,y);
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position) && mListener != null) {
            mListener.onDoneClicked(cursor.getLong(COL_ID),isDone);
            mActivatedPosition = position;
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

    private void saveNewItem() {
        String title = mNewItemText.getText().toString();
        if (title == null || title.length() == 0) {
            Toast.makeText(getActivity(),"Item title can't be empty",Toast.LENGTH_LONG).show();
            return;
        }

        int quantity = 1;
        String quantiryStr = mNewItemQuantity.getText().toString();
        if (quantiryStr != null && quantiryStr.length() > 0) {
            try {
                quantity = Integer.parseInt(quantiryStr);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        boolean isUrgent = mNewItemUrgent.isChecked();

        ContentValues values = new ContentValues();
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_TITLE,title);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY,quantity);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_URGENT,isUrgent);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY, DoUIParseSyncAdapter.getUserId());
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_DONE,false);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_IS_DIRTY,true);
        values.put(DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID,DoUIContract.NOT_SYNCED);

        Uri newRow = getActivity().getContentResolver().insert(DoUIContract.ShoppingItemEntry.CONTENT_URI,values);
        if (isUrgent) {
            long id = ContentUris.parseId(newRow);
            SyncDoneReceiver receiver = new SyncDoneReceiver(null,DoUIPushBroadcastReceiver.PUSH_CODE_URGENT_SHOPPING,id);
            getActivity().getApplicationContext().registerReceiver(receiver,new IntentFilter(R.string.broadcast_sync_done+"."+DoUIContract.PATH_SHOPPING));
        }
        DoUISyncAdapter.syncImmediately(getActivity().getApplicationContext(),DoUIContract.PATH_SHOPPING);

        mNewItemLayout.setVisibility(View.GONE);
        mNewItemUrgent.setChecked(false);
        mNewItemQuantity.setText("");
        mNewItemText.setText("");
        mNewItemButton.setVisibility(View.VISIBLE);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                DoUIContract.ShoppingItemEntry.CONTENT_URI,
                SHOPPING_PROJECTION,
                null,
                null,
                DoUIContract.ShoppingItemEntry.COLUMN_URGENT+" DESC"
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
