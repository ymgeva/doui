package com.ymgeva.doui.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yoav on 4/14/15.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "doui.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TASKS_TABLE = "CREATE TABLE " + DoUIContract.TaskItemEntry.TABLE_NAME + " (" +
                DoUIContract.TaskItemEntry._ID + " INTEGER PRIMARY KEY," +
                DoUIContract.TaskItemEntry.COLUMN_PARSE_ID + " TEXT NOT NULL, " +
                DoUIContract.TaskItemEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                DoUIContract.TaskItemEntry.COLUMN_TEXT + " TEXT, " +
                DoUIContract.TaskItemEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE + " INTEGER NOT NULL, " +
                DoUIContract.TaskItemEntry.COLUMN_REMINDER + " INTEGER NOT NULL, " +
                DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME + " INTEGER, " +
                DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO + " TEXT NOT NULL, " +
                DoUIContract.TaskItemEntry.COLUMN_CREATED_BY + " TEXT NOT NULL, " +
                DoUIContract.TaskItemEntry.COLUMN_IMAGE + " BLOB, " +
                DoUIContract.TaskItemEntry.COLUMN_DONE + " INTEGER NOT NULL, " +
                "UNIQUE (" + DoUIContract.TaskItemEntry.COLUMN_PARSE_ID +") ON CONFLICT REPLACE"+
                " );";

        final String SQL_CREATE_SHOPPING_ITEMS_TABLE = "CREATE TABLE " + DoUIContract.ShoppingItemEntry.TABLE_NAME + " (" +
                DoUIContract.ShoppingItemEntry._ID + " INTEGER PRIMARY KEY," +
                DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID + " TEXT NOT NULL, " +
                DoUIContract.ShoppingItemEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY + " INTEGER, " +
                DoUIContract.ShoppingItemEntry.COLUMN_URGENT + " INTEGER NOT NULL, " +
                DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY + " TEXT NOT NULL, " +
                DoUIContract.ShoppingItemEntry.COLUMN_DONE + " INTEGER NOT NULL, " +
                "UNIQUE (" + DoUIContract.ShoppingItemEntry.COLUMN_PARSE_ID +") ON CONFLICT REPLACE"+
                " );";

        final String SQL_CREATE_GENERAL_ITEMS_TABLE = "CREATE TABLE " + DoUIContract.GeneralItemEntry.TABLE_NAME + " (" +
                DoUIContract.GeneralItemEntry._ID + " INTEGER PRIMARY KEY," +
                DoUIContract.GeneralItemEntry.COLUMN_PARSE_ID + " TEXT NOT NULL, " +
                DoUIContract.GeneralItemEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                DoUIContract.GeneralItemEntry.COLUMN_ASSIGNED_TO + " TEXT NOT NULL, " +
                DoUIContract.GeneralItemEntry.COLUMN_URGENT + " INTEGER NOT NULL, " +
                DoUIContract.GeneralItemEntry.COLUMN_NOTIFY_WHEN_DONE + " INTEGER NOT NULL, " +
                DoUIContract.GeneralItemEntry.COLUMN_CREATED_BY + " TEXT NOT NULL, " +
                DoUIContract.GeneralItemEntry.COLUMN_DONE + " INTEGER NOT NULL, " +
                "UNIQUE (" + DoUIContract.GeneralItemEntry.COLUMN_PARSE_ID +") ON CONFLICT REPLACE"+
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_TASKS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SHOPPING_ITEMS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_GENERAL_ITEMS_TABLE);




    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DoUIContract.TaskItemEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DoUIContract.ShoppingItemEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DoUIContract.GeneralItemEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
