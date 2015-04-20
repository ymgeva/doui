package com.ymgeva.doui.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Yoav on 4/14/15.
 */
public class DoUIContract {

    public static final String CONTENT_AUTHORITY = "com.ymgeva.doui";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TASKS = "tasks";
    public static final String PATH_SHOPPING = "shopping";
    public static final String PATH_GENERAL = "general";



    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getDbDateString(Date date){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }
    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }




    public static final class TaskItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_TASKS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_TASKS;

        public static final String TABLE_NAME = "task_items";

        public static final String COLUMN_PARSE_ID = "parse_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_CREATED_BY = "created_by";
        public static final String COLUMN_ASSIGNED_TO = "assigned_to";
        public static final String COLUMN_REMINDER = "reminder";
        public static final String COLUMN_REMINDER_TIME = "reminder_time";
        public static final String COLUMN_TEXT = "task_text";
        public static final String COLUMN_NOTIFY_WHEN_DONE = "notify_when_done";
        public static final String COLUMN_IMAGE = "task_image";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DONE = "done";


        public static Uri buildTaskUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class ShoppingItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOPPING).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING;
        public static final String TABLE_NAME = "shopping_items";

        public static final String COLUMN_PARSE_ID = "parse_id";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_CREATED_BY = "created_by";
        public static final String COLUMN_URGENT = "urgent";
        public static final String COLUMN_DONE = "done";


        public static Uri buildShoppingItemUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class GeneralItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GENERAL).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING;
        public static final String TABLE_NAME = "general_items";

        public static final String COLUMN_PARSE_ID = "parse_id";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_CREATED_BY = "created_by";
        public static final String COLUMN_ASSIGNED_TO = "assigned_to";
        public static final String COLUMN_URGENT = "urgent";
        public static final String COLUMN_NOTIFY_WHEN_DONE = "notify_when_done";
        public static final String COLUMN_DONE = "done";



        public static Uri buildGeneralItemUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
