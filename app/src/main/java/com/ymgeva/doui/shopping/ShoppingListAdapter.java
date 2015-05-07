package com.ymgeva.doui.shopping;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ymgeva.doui.R;
import com.ymgeva.doui.uiobjects.CustomCheckBox;

/**
 * Created by Yoav on 4/26/15.
 */
public class ShoppingListAdapter extends CursorAdapter {

    private static final String LOG_TAG = ShoppingListAdapter.class.getSimpleName();

    private Context mContext;

    public static class ViewHolder {
        public final TextView mTitleTextView;
        public final CustomCheckBox mDoneCheckBox;
        public long mId;

        public ViewHolder(View view) {
            mDoneCheckBox = (CustomCheckBox)view.findViewById(R.id.shopping_item_done_checkbox);
            mTitleTextView = (TextView)view.findViewById(R.id.shopping_item_title_text_view);
        }

    }

    public ShoppingListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.shopping_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.mId = cursor.getLong(ShoppingListFragment.COL_ID);

        viewHolder.mTitleTextView.setText(cursor.getString(ShoppingListFragment.COL_TITLE));

        boolean isUrgent = cursor.getInt(ShoppingListFragment.COL_URGENT) > 0;
        boolean isDone = cursor.getInt(ShoppingListFragment.COL_DONE) > 0;
        viewHolder.mDoneCheckBox.setChecked(isDone);
        if (isDone) {
            viewHolder.mTitleTextView.setPaintFlags(viewHolder.mTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.mTitleTextView.setTextColor(context.getResources().getColor(R.color.primary_text_disabled_material_light));

        }
        else {
            viewHolder.mTitleTextView.setPaintFlags(viewHolder.mTitleTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.mTitleTextView.setPaintFlags(viewHolder.mTitleTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

            if (isUrgent) {
                viewHolder.mTitleTextView.setTextColor(context.getResources().getColor(R.color.urgent_item));
            }
            else {
                viewHolder.mTitleTextView.setTextColor(context.getResources().getColor(R.color.primary_dark_material_light));
            }

        }

        viewHolder.mDoneCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ShoppingListActivity) mContext).onDoneClicked(viewHolder.mId,viewHolder.mDoneCheckBox.isChecked());

            }
        });


    }
}
