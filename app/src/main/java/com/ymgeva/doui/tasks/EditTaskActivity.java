package com.ymgeva.doui.tasks;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ymgeva.doui.R;
import com.ymgeva.doui.Utility;
import com.ymgeva.doui.data.DoUIContract;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;
import com.ymgeva.doui.sync.DoUISyncAdapter;

import java.util.Calendar;
import java.util.Date;

public class EditTaskActivity extends ActionBarActivity {

    public static final String IS_NEW_TASK_SETTING = "mode_setting";
    private EditTaskFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        if (savedInstanceState == null) {
            mFragment = new EditTaskFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            mFragment.onSaveClicked();
        }
        else if (id == R.id.action_cancel) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public static class EditTaskFragment extends Fragment {

        private static final long DEFAULT_REMINDER_TIME = 1000*60*15;

        private boolean isNewTaskMode;
        private boolean isCreatedByMe;

        private long mId;
        private String mParseId;
        private long mDate;
        private long mReminderTime;
        private String mAssignedTo;
        private String mCreatedBy;

        private EditText mTitleView;
        private TextView mDateView;
        private TextView mTimeView;
        private ImageView mCreatedByView;
        private ImageView mAssignedToView;
        private TextView mDescriptionView;
        private CheckBox mReminderView;
        private CheckBox mNotifyWhenDoneView;
        private TextView mReminderTimeView;

        public EditTaskFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 final Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_task, container, false);

            mTitleView = (EditText)rootView.findViewById(R.id.edit_task_title);
            mDescriptionView = (TextView) rootView.findViewById(R.id.edit_task_text);
            mDateView = (TextView) rootView.findViewById(R.id.edit_task_date);
            mTimeView = (TextView) rootView.findViewById(R.id.edit_task_time);
            mCreatedByView = (ImageView) rootView.findViewById(R.id.edit_task_from_image);
            mAssignedToView = (ImageView) rootView.findViewById(R.id.edit_task_to_image);
            mReminderView = (CheckBox) rootView.findViewById(R.id.edit_task_reminder_checkbox);
            mReminderTimeView = (TextView) rootView.findViewById(R.id.edit_task_reminder_time);
            mNotifyWhenDoneView = (CheckBox) rootView.findViewById(R.id.edit_task_notify_checkbox);

            Intent intent = getActivity().getIntent();
            isNewTaskMode = intent.getBooleanExtra(IS_NEW_TASK_SETTING, true);

            if (!isNewTaskMode) {
                mId = intent.getLongExtra(DoUIContract.TaskItemEntry._ID,0);
                mParseId = intent.getStringExtra(DoUIContract.TaskItemEntry.COLUMN_PARSE_ID);

                mDate = intent.getLongExtra(DoUIContract.TaskItemEntry.COLUMN_DATE, new Date().getTime());
                mDateView.setText(Utility.formatShortDate(mDate));
                mTimeView.setText(Utility.formatTime(mDate));

                mTitleView.setText(intent.getStringExtra(DoUIContract.TaskItemEntry.COLUMN_TITLE));
                mDescriptionView.setText(intent.getStringExtra(DoUIContract.TaskItemEntry.COLUMN_DESCRIPTION));


                mCreatedBy = intent.getStringExtra(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY);
                isCreatedByMe = (mCreatedBy.equals(DoUIParseSyncAdapter.getUserId()));
                mCreatedByView.setImageResource(Utility.imageResourseByUser(mCreatedBy));
                mAssignedTo = intent.getStringExtra(DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO);
                mAssignedToView.setImageResource(Utility.imageResourseByUser(mAssignedTo));

                boolean reminder = intent.getBooleanExtra(DoUIContract.TaskItemEntry.COLUMN_REMINDER, false);
                mReminderView.setChecked(reminder);
                if (reminder) {
                    mReminderTimeView.setVisibility(View.VISIBLE);
                    mReminderTime = intent.getLongExtra(DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME,mDate-DEFAULT_REMINDER_TIME);
                    mReminderTimeView.setText(Utility.formatDateTime(mReminderTime));
                }
                else {
                    mReminderTimeView.setVisibility(View.INVISIBLE);
                    mReminderTime = new Date().getTime()-DEFAULT_REMINDER_TIME;
                }
                mNotifyWhenDoneView.setChecked(intent.getBooleanExtra(DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE,false));
            }
            else {
                mAssignedTo = null;
                mDate = new Date().getTime();
                mReminderView.setChecked(false);
                mReminderTimeView.setVisibility(View.INVISIBLE);
                mNotifyWhenDoneView.setChecked(false);
                mCreatedBy = DoUIParseSyncAdapter.getUserId();
                mCreatedByView.setImageResource(Utility.imageResourseByUser(mCreatedBy));
            }



            mDateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(mDate));
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar newCal = Calendar.getInstance();
                            newCal.set(year, monthOfYear, dayOfMonth);

                            Calendar old = Calendar.getInstance();
                            old.setTime(new Date(mDate));

                            newCal.set(Calendar.HOUR_OF_DAY,old.get(Calendar.HOUR_OF_DAY));
                            newCal.set(Calendar.MINUTE,old.get(Calendar.MINUTE));

                            mDate = newCal.getTime().getTime();
                            mDateView.setText(Utility.formatShortDate(mDate));
                        }
                    },calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.show();
                }
            });

            mTimeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(mDate));
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            Calendar newCal = Calendar.getInstance();
                            Calendar oldCal = Calendar.getInstance();
                            oldCal.setTime(new Date(mDate));

                            newCal.set(oldCal.get(Calendar.YEAR),oldCal.get(Calendar.MONTH),oldCal.get(Calendar.DAY_OF_MONTH),hour,minute);
                            mDate = newCal.getTime().getTime();
                            mTimeView.setText(Utility.formatTime(mDate));
                        }
                    },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true);
                    timePickerDialog.show();
                }
            });


            mReminderTimeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(mReminderTime));
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            Calendar newCal = Calendar.getInstance();
                            Calendar oldCal = Calendar.getInstance();
                            oldCal.setTime(new Date(mReminderTime));

                            newCal.set(oldCal.get(Calendar.YEAR),oldCal.get(Calendar.MONTH),oldCal.get(Calendar.DAY_OF_MONTH),hour,minute);
                            mReminderTime = newCal.getTime().getTime();
                            mReminderTimeView.setText(Utility.formatTime(mReminderTime));
                        }
                    },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true);
                    timePickerDialog.show();
                }
            });


            mAssignedToView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View spinnerView = getLayoutInflater(savedInstanceState).inflate(R.layout.i_u_spinner_dialog,null);
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setView(spinnerView);

                    IUSPinnerAdapter adapter = new IUSPinnerAdapter(getActivity(),R.layout.i_u_spinner_dialog);
                    adapter.add("I");
                    adapter.add("U");

                    dialogBuilder.setAdapter(adapter,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                mAssignedTo = DoUIParseSyncAdapter.getUserId();
                            } else {
                                mAssignedTo = DoUIParseSyncAdapter.getPartnerId();
                            }
                            mAssignedToView.setImageResource(Utility.imageResourseByUser(mAssignedTo));
                        }
                    });

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);

                }
            });

            mReminderView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        mReminderTimeView.setVisibility(View.VISIBLE);
                        mReminderTimeView.performClick();
                    }
                    else {
                        mReminderTimeView.setVisibility(View.INVISIBLE);
                    }
                }
            });

            return rootView;
        }

        public class IUSPinnerAdapter extends ArrayAdapter<String> {

            int [] texts = {R.string.i_string,R.string.u_string};
            int [] icons = {R.drawable.i_image,R.drawable.u_image};

            public IUSPinnerAdapter(Context context, int resource) {
                super(context, resource);
            }


            @Override
            public View getDropDownView(int position, View view, ViewGroup parent) {
                return getIUView(position, view, parent);
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                return getIUView(position, view, parent);
            }

            public View getIUView(int position, View view, ViewGroup parent) {

                LayoutInflater inflater = getLayoutInflater(getArguments());
                View spinner = inflater.inflate(R.layout.i_u_spinner, parent, false);

                TextView textView = (TextView) spinner.findViewById(R.id.spinner_text);
                textView.setText(texts[position]);

                ImageView imageView = (ImageView) spinner.findViewById(R.id.spinner_icon);
                imageView.setImageResource(icons[position]);

                return spinner;

            }
        }

        public void onSaveClicked() {

            //check that must fields are full
            StringBuilder sb = new StringBuilder();
            if (mTitleView.getText() == null) sb.append("Title, ");
            if (mDateView.getText() == null) sb.append("Date, ");
            if (mTimeView.getText() == null) sb.append("Time, ");
            if (mAssignedTo == null) sb.append("Task Assignee");

            if (sb.length() > 0) {
                sb.append("!");
                Toast.makeText(getActivity(),"Following details must not be empty: "+sb.toString(),Toast.LENGTH_LONG).show();
                return;
            }

            else {
                ContentValues values = new ContentValues();
                values.put(DoUIContract.TaskItemEntry.COLUMN_DATE,mDate);
                values.put(DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO,mAssignedTo);
                values.put(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,mCreatedBy);
                values.put(DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE,mNotifyWhenDoneView.isChecked());
                values.put(DoUIContract.TaskItemEntry.COLUMN_DONE,false);
                values.put(DoUIContract.TaskItemEntry.COLUMN_IS_DIRTY,true);
                if (mReminderView.isChecked()) {
                    values.put(DoUIContract.TaskItemEntry.COLUMN_REMINDER,true);
                    values.put(DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME,mReminderTime);
                }
                else {
                    values.put(DoUIContract.TaskItemEntry.COLUMN_REMINDER,false);
                }
                values.put(DoUIContract.TaskItemEntry.COLUMN_DESCRIPTION,mDescriptionView.getText().toString());
                values.put(DoUIContract.TaskItemEntry.COLUMN_TITLE,mTitleView.getText().toString());

                if (isNewTaskMode) {
                    values.put(DoUIContract.TaskItemEntry.COLUMN_PARSE_ID, DoUIContract.NOT_SYNCED);
                    getActivity().getContentResolver().insert(DoUIContract.TaskItemEntry.CONTENT_URI,values);
                }
                else {
                    values.put(DoUIContract.TaskItemEntry.COLUMN_PARSE_ID, mParseId);
                    values.put(DoUIContract.TaskItemEntry._ID,mId);
                    getActivity().getContentResolver().update(DoUIContract.TaskItemEntry.CONTENT_URI,values,"_ID = "+mId,null);
                }
                DoUISyncAdapter.syncImmediately(getActivity().getApplicationContext(), DoUIContract.PATH_TASKS);
                getActivity().finish();
            }
        }


    }


}
