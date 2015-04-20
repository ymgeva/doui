package com.ymgeva.doui.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Yoav on 4/14/15.
 */
public class DoUIContentProvider extends ContentProvider {


    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DbHelper mOpenHelper;

    private static final int TASKS = 100;
    private static final int SHOPPING = 200;
    private static final int GENERAL = 300;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DoUIContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DoUIContract.PATH_TASKS, TASKS);
        matcher.addURI(authority, DoUIContract.PATH_SHOPPING,SHOPPING);
        matcher.addURI(authority, DoUIContract.PATH_GENERAL,GENERAL);


        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case TASKS: {
                builder.setTables(DoUIContract.TaskItemEntry.TABLE_NAME);
                break;
            }
            case SHOPPING: {
                builder.setTables(DoUIContract.ShoppingItemEntry.TABLE_NAME);
                break;
            }
            case GENERAL: {
                builder.setTables(DoUIContract.GeneralItemEntry.TABLE_NAME);
                break;
            }
            default: {
                return null;
            }
        }

        return builder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
     }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:return DoUIContract.TaskItemEntry.CONTENT_TYPE;
            case SHOPPING:return DoUIContract.ShoppingItemEntry.CONTENT_TYPE;
            case GENERAL:return DoUIContract.GeneralItemEntry.CONTENT_TYPE;

        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TASKS: {
                long _id = db.insert(DoUIContract.TaskItemEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DoUIContract.TaskItemEntry.buildTaskUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SHOPPING: {
                long _id = db.insert(DoUIContract.ShoppingItemEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DoUIContract.ShoppingItemEntry.buildShoppingItemUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case GENERAL: {
                long _id = db.insert(DoUIContract.GeneralItemEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DoUIContract.GeneralItemEntry.buildGeneralItemUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }



        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case TASKS: {
                rowsDeleted = db.delete(
                        DoUIContract.TaskItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case SHOPPING: {
                rowsDeleted = db.delete(
                        DoUIContract.ShoppingItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case GENERAL: {
                rowsDeleted = db.delete(
                        DoUIContract.GeneralItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case TASKS: {
                rowsUpdated = db.update(DoUIContract.TaskItemEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }
            case SHOPPING: {
                rowsUpdated = db.update(DoUIContract.ShoppingItemEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }
            case GENERAL: {
                rowsUpdated = db.update(DoUIContract.GeneralItemEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String tableName;
        switch (match) {
            case TASKS: {
                tableName = DoUIContract.TaskItemEntry.TABLE_NAME;
                break;
            }
            case SHOPPING: {
                tableName = DoUIContract.ShoppingItemEntry.TABLE_NAME;
                break;
            }
            case GENERAL: {
                tableName = DoUIContract.GeneralItemEntry.TABLE_NAME;
                break;
            }

            default:
                return super.bulkInsert(uri, values);
        }

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }
}
