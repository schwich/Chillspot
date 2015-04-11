package com.jordanschwichtenberg.chillspot.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Jordan on 4/11/2015.
 */
public class EventProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private EventDBHelper mOpenHelper;

    static final int EVENTS = 100;
    static final int USER = 200;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = EventContract.CONTENT_AUTHORITY;

        // URI to return all events
        matcher.addURI(authority, EventContract.PATH_EVENTS, EVENTS);
        // URI to return the user's details
        matcher.addURI(authority, EventContract.PATH_USER, USER);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new EventDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor returnedCursor;

        switch (sUriMatcher.match(uri)) {
            case EVENTS:
            {
                returnedCursor = mOpenHelper.getReadableDatabase().query(
                        EventContract.EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case USER:
            {
                returnedCursor = mOpenHelper.getReadableDatabase().query(
                        EventContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        returnedCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnedCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case EVENTS:
                return EventContract.EventEntry.CONTENT_TYPE;
            case USER:
                return EventContract.UserEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnedUri;

        switch (match) {
            case EVENTS:
            {
                long _id = db.insert(EventContract.EventEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnedUri = EventContract.EventEntry.buildEventUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case USER:
            {
                long _id = db.insert(EventContract.UserEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnedUri = EventContract.UserEntry.buildUserUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (selection == null) selection = "1";

        switch (match) {
            case EVENTS:
            {
                rowsDeleted = db.delete(EventContract.EventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case USER:
            {
                rowsDeleted = db.delete(EventContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case EVENTS:
            {
                rowsUpdated = db.update(EventContract.EventEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            }

            case USER:
            {
                rowsUpdated = db.update(EventContract.UserEntry.TABLE_NAME,
                        values, selection, selectionArgs);
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

        switch (match) {
            case EVENTS:
            {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(EventContract.EventEntry.TABLE_NAME, null, value);
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

            default:
                return super.bulkInsert(uri, values);
        }
    }
}
