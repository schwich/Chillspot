package com.jordanschwichtenberg.chillspot.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.jordanschwichtenberg.chillspot.R;
import com.jordanschwichtenberg.chillspot.data.EventContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Jordan on 4/11/2015.
 */
public class ChillspotSyncAdapter extends AbstractThreadedSyncAdapter {

    // sync every hour
    public static final int SYNC_INTERVAL = 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public final String LOG_TAG = ChillspotSyncAdapter.class.getSimpleName();

    public ChillspotSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (accountManager.getPassword(newAccount) == null) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */


            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        ChillspotSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.d(LOG_TAG, "Starting sync.");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        URL url = null;

        // Will contain the raw JSON response as a string.
        String eventsListJsonStr = null;

        // create connection to chillspot api, and open the connection
        try {
            // TODO: add location query params
            url = new URL("http://evening-harbor-2864.herokuapp.com/events");

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // read input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // adds newline for debugging purposes
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream empty, don't parse
                return;
            }

            eventsListJsonStr = buffer.toString();
            getEventDataFromJSON(eventsListJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR: ", e);
            Log.e(LOG_TAG, urlConnection.getErrorStream().toString());
            return;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getEventDataFromJSON(String jsonStr) throws JSONException {

        final String CHILLSPOT_EVENT_ID = "id";
        final String CHILLSPOT_EVENT_ADDRESS = "address";
        final String CHILLSPOT_EVENT_LATITUDE = "latitude";
        final String CHILLSPOT_EVENT_LONGITUDE = "longitude";
        final String CHILLSPOT_EVENT_CREATED_AT = "created_at";
        final String CHILLSPOT_EVENT_DISTANCE = "distance";
        final String CHILLSPOT_EVENT_CATEGORY = "category";
        final String CHILLSPOT_EVENT_SUB_CATEGORY = "sub_category";
        final String CHILLSPOT_EVENT_NOTE = "note";

        JSONArray eventsList = new JSONArray(jsonStr);

        Vector<ContentValues> contentValuesVector = new Vector<>(eventsList.length());

        for (int i = 0; i < eventsList.length(); i++) {

            long id;
            String address;
            double latitude;
            double longitude;
            String created_at;
            double distance;
            String category;
            String sub_category;
            String note;

            // Get the JSON data
            JSONObject eventObject = eventsList.getJSONObject(i);
            id = eventObject.getLong(CHILLSPOT_EVENT_ID);
            address = eventObject.getString(CHILLSPOT_EVENT_ADDRESS);
            latitude = eventObject.getDouble(CHILLSPOT_EVENT_LATITUDE);
            longitude = eventObject.getDouble(CHILLSPOT_EVENT_LONGITUDE);
            created_at = eventObject.getString(CHILLSPOT_EVENT_CREATED_AT);
            // TODO: add this when location query params are added
            //distance = eventObject.getDouble(CHILLSPOT_EVENT_DISTANCE);
            category = eventObject.getString(CHILLSPOT_EVENT_CATEGORY);
            sub_category = eventObject.getString(CHILLSPOT_EVENT_SUB_CATEGORY);
            note = eventObject.getString(CHILLSPOT_EVENT_NOTE);

            ContentValues eventValues = new ContentValues();

            // store each row
            eventValues.put(EventContract.EventEntry.COLUMN_EVENT_ID, id);
            eventValues.put(EventContract.EventEntry.COLUMN_ADDRESS, address);
            eventValues.put(EventContract.EventEntry.COLUMN_LATITUDE, latitude);
            eventValues.put(EventContract.EventEntry.COLUMN_LONGITUDE, longitude);
            eventValues.put(EventContract.EventEntry.COLUMN_CREATED_AT, created_at);
            // TODO: add this when location query params are added
            //eventValues.put(EventContract.EventEntry.COLUMN_DISTANCE, distance);
            eventValues.put(EventContract.EventEntry.COLUMN_CATEGORY, category);
            eventValues.put(EventContract.EventEntry.COLUMN_SUB_CATEGORY, sub_category);
            eventValues.put(EventContract.EventEntry.COLUMN_NOTE, note);

            // add each row to the vector containing all rows
            contentValuesVector.add(eventValues);
        }

        int inserted = 0;

        // add to db
        if (contentValuesVector.size() > 0) {

            // delete old data as this is just a cache
            // TODO: instead of deleting all data, only update those that have changed, and delete those who have been removed
            getContext().getContentResolver().delete(EventContract.EventEntry.CONTENT_URI, null, null);

            ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(contentValuesArray);
            getContext().getContentResolver().bulkInsert(EventContract.EventEntry.CONTENT_URI, contentValuesArray);
        }

        Log.d(LOG_TAG, "Sync complete. " + contentValuesVector.size() + " Inserted.");
    }
}
