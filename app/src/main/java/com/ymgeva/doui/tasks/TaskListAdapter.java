package com.ymgeva.doui.tasks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ymgeva.doui.R;
import com.ymgeva.doui.Utility;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;

/**
 * Created by Yoav on 4/20/15.
 */
public class TaskListAdapter extends CursorAdapter  {

    private static final String LOG_TAG = TaskListAdapter.class.getSimpleName();

    public static class ViewHolder {
        public final ImageView mImageView;
        public final TextView mDateTextView;
        public final TextView mTimeTextView;
        public final TextView mTitleTextView;

        public ViewHolder(View view) {
            mImageView = (ImageView)view.findViewById(R.id.list_item_icon);
            mDateTextView = (TextView)view.findViewById(R.id.list_item_date_textview);
            mTimeTextView = (TextView)view.findViewById(R.id.list_item_time_textview);
            mTitleTextView = (TextView)view.findViewById(R.id.list_item_title_text_view);
        }
    }

    public TaskListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_list_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String assignedTo = cursor.getString(TaskListFragment.COL_ASSIGNED_TO);
        if (assignedTo.equals(DoUIParseSyncAdapter.getInstance().getUserId())) {
            viewHolder.mImageView.setImageResource(R.drawable.i_image);
        }
        else {
            viewHolder.mImageView.setImageResource(R.drawable.u_image);
        }

        long date = cursor.getLong(TaskListFragment.COL_DATE);
        viewHolder.mDateTextView.setText(Utility.formatShortDate(date));
        viewHolder.mTimeTextView.setText(Utility.formatTime(date));

        viewHolder.mTitleTextView.setText(cursor.getString(TaskListFragment.COL_TITLE));

        boolean isDone = cursor.getInt(TaskListFragment.COL_DONE) > 0;
        if (isDone) {
            viewHolder.mTitleTextView.setPaintFlags(viewHolder.mTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.mTitleTextView.setTextColor(context.getResources().getColor(R.color.primary_text_disabled_material_light));

            viewHolder.mDateTextView.setPaintFlags(viewHolder.mDateTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.mDateTextView.setTextColor(context.getResources().getColor(R.color.primary_text_disabled_material_light));

            viewHolder.mTimeTextView.setPaintFlags(viewHolder.mTimeTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.mTimeTextView.setTextColor(context.getResources().getColor(R.color.primary_text_disabled_material_light));
        }
        else {
            viewHolder.mTitleTextView.setPaintFlags(viewHolder.mTitleTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.mTitleTextView.setTextColor(context.getResources().getColor(R.color.primary_dark_material_light));

            viewHolder.mDateTextView.setPaintFlags(viewHolder.mDateTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.mDateTextView.setTextColor(context.getResources().getColor(R.color.primary_dark_material_light));

            viewHolder.mTimeTextView.setPaintFlags(viewHolder.mTimeTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.mTimeTextView.setTextColor(context.getResources().getColor(R.color.primary_dark_material_light));
        }

    }
}
