package com.jordanschwichtenberg.chillspot.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jordanschwichtenberg.chillspot.data.EventContract.EventEntry;
import com.jordanschwichtenberg.chillspot.data.EventContract.UserEntry;

/**
 * Manages a local database that stores event details, and the user's details.
 */
public class EventDBHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "events.db";
    /**
     * Don't forget to change this version number when you alter the schema!!!
     * onUpdate() won't be called unless you do this!!!
     */
    private static final int DATABASE_VERSION = 2;

    public EventDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a table to hold events
        final String SQL_CREATE_EVENTS_TABLE = "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
                EventEntry._ID + " INTEGER PRIMARY KEY," +
                EventEntry.COLUMN_EVENT_ID + " INTEGER UNIQUE NOT NULL," +
                EventEntry.COLUMN_ADDRESS + " TEXT," +
                EventEntry.COLUMN_LATITUDE + " REAL NOT NULL," +
                EventEntry.COLUMN_LONGITUDE + " REAL NOT NULL," +
                EventEntry.COLUMN_CREATED_AT + " TEXT," +
                EventEntry.COLUMN_DISTANCE + " REAL," +
                EventEntry.COLUMN_CATEGORY + " TEXT," +
                EventEntry.COLUMN_SUB_CATEGORY + " TEXT," +
                EventEntry.COLUMN_NOTE + " TEXT," +
                EventEntry.COLUMN_PEOPLE_ATTENDING + " INTEGER " + " );";

        // Create table to store user info
        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY," +
                UserEntry.COLUMN_USER_ID + " INTEGER NOT NULL," +
                UserEntry.COLUMN_EVENT_ID + " INTEGER," +
                UserEntry.COLUMN_PEOPLE_ATTENDING + " INTEGER" + " );";

        db.execSQL(SQL_CREATE_EVENTS_TABLE);
        Log.v("WHAT", "Why wasn't people_attending column not created? : " + SQL_CREATE_EVENTS_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
    }

    /**
     * This won't be called unless you change DATABASE_VERSION above!!!
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply discard the data and start over.
        db.execSQL("DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);

        // create new tables
        onCreate(db);
    }
}
