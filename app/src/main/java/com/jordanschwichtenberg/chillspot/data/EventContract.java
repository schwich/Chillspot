package com.jordanschwichtenberg.chillspot.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Jordan on 4/11/2015.
 */
public class EventContract {

    // Uniquely defines this content provider
    public static final String CONTENT_AUTHORITY = "com.jordanschwichtenberg.chillspot";

    // content://com.jordanschwichtenberg.chillspot
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // possible table paths
    public static final String PATH_EVENTS = "events";
    public static final String PATH_USER = "user";

    /**
     * Defines the contents of the events table
     */
    public static final class EventEntry implements BaseColumns {

        // content://com.jordanschwichtenberg.chillspot/events
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();
        /**
         * Define the events table columns
         */

        public static final String TABLE_NAME = "events";
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_SUB_CATEGORY = "sub_category";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_PEOPLE_ATTENDING = "people_attending";

        public static Uri buildEventUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }


    }

    /**
     * Defines the content of the user table
     */
    public static final class UserEntry implements BaseColumns {

        // content://com.jordanschwichtenberg.chillspot/user
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();
        /**
         * Define the user table columns
         */

        public static final String TABLE_NAME = "user";
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String COLUMN_USER_ID = "user_id";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String COLUMN_PEOPLE_ATTENDING = "people_attending";

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }

}
