package com.jordanschwichtenberg.chillspot;

/**
 * Created by Jordan on 3/18/2015.
 */

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jordanschwichtenberg.chillspot.data.EventContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EventDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EVENT_DETAIL_URI = "URI";

    private Uri mUri;

    private static final int EVENT_DETAIL_LOADER = 0;

    private static final String[] EVENT_DETAIL_COLUMNS = {
        EventContract.EventEntry.TABLE_NAME + "." + EventContract.EventEntry._ID,
        EventContract.EventEntry.COLUMN_EVENT_ID,
        EventContract.EventEntry.COLUMN_ADDRESS,
        EventContract.EventEntry.COLUMN_LATITUDE,
        EventContract.EventEntry.COLUMN_LONGITUDE,
        EventContract.EventEntry.COLUMN_CREATED_AT,
        EventContract.EventEntry.COLUMN_DISTANCE,
        EventContract.EventEntry.COLUMN_CATEGORY,
        EventContract.EventEntry.COLUMN_SUB_CATEGORY,
        EventContract.EventEntry.COLUMN_NOTE
    };

    static final int COL__ID = 0;
    static final int COL_EVENT_ID = 1;
    static final int COL_EVENT_ADDRESS = 2;
    static final int COL_EVENT_LATITUDE = 3;
    static final int COL_EVENT_LONGITUDE = 4;
    static final int COL_EVENT_CREATED_AT = 5;
    static final int COL_EVENT_DISTANCE = 6;
    static final int COL_EVENT_CATEGORY = 7;
    static final int COL_EVENT_SUB_CATEGORY = 8;
    static final int COL_EVENT_NOTE = 9;

    private TextView mSubCategoryView;
    private TextView mCategoryView;
    private TextView mAddressView;
    private TextView mDistanceView;
    private TextView mCreatedAtView;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mNoteView;
    private Button mJoinEventButton;

    public EventDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(EVENT_DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);

        mSubCategoryView = (TextView) rootView.findViewById(R.id.detail_subcategory_textview);
        mCategoryView = (TextView) rootView.findViewById(R.id.detail_category_textview);
        mAddressView = (TextView) rootView.findViewById(R.id.detail_address_textview);
        mDistanceView = (TextView) rootView.findViewById(R.id.detail_distance_textview);
        mCreatedAtView = (TextView) rootView.findViewById(R.id.detail_created_at_textview);
        mLatitudeView = (TextView) rootView.findViewById(R.id.detail_lat_textview);
        mLongitudeView = (TextView) rootView.findViewById(R.id.detail_lon_textview);
        mNoteView = (TextView) rootView.findViewById(R.id.detail_note_textview);

        mJoinEventButton = (Button) rootView.findViewById(R.id.detail_join_button);
        mJoinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: make API call to join event, then transition to the YourEventFragment(from the MainActivity)
                JoinEventTask apiCall = new JoinEventTask();
                apiCall.execute();
            }
        });

        /*// kick off async task
        FetchEventDetailTask eventTask = new FetchEventDetailTask();
        eventTask.execute();*/

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(EVENT_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(getActivity(), mUri, EVENT_DETAIL_COLUMNS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            String subcat = data.getString(COL_EVENT_SUB_CATEGORY);
            mSubCategoryView.setText(subcat);

            String cat = data.getString(COL_EVENT_CATEGORY);
            mCategoryView.setText(cat);

            String address = data.getString(COL_EVENT_ADDRESS);
            mAddressView.setText("Address:\n" + address);

            // TODO: set distance to human readable format
            Double distance = data.getDouble(COL_EVENT_DISTANCE);
            //String formatted_distance = Utility.formatDistance(getActivity(), distance);
            mDistanceView.setText("Distance:\n" + Double.toString(distance));

            String created_at = data.getString(COL_EVENT_CREATED_AT);
            mCreatedAtView.setText("Created At:\n" + created_at);

            Double lat = data.getDouble(COL_EVENT_LATITUDE);
            mLatitudeView.setText("Latitude: " + Double.toString(lat));

            Double lon = data.getDouble(COL_EVENT_LONGITUDE);
            mLongitudeView.setText("Longitude: " + Double.toString(lon));

            String note = data.getString(COL_EVENT_NOTE);
            mNoteView.setText("Note\n" + note);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * This will send a POST request to the server that joins the user to the particular event
     */
    public class JoinEventTask extends AsyncTask<Void, Void, String> {

        final String CHILLSPOT_STATUS_CODE = "status";
        private final String LOG_TAG = JoinEventTask.class.getSimpleName();

        private String getReturnedJSON(String jsonStr) throws JSONException {

            JSONObject data = new JSONObject(jsonStr);

            String statusCode = data.getString(CHILLSPOT_STATUS_CODE);

            String resultStr = "Status:\t" + statusCode;



            if (statusCode.equals("201")) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("event_id_of_user", Integer.parseInt(EventContract.EventEntry.getIdFromUri(mUri)));
                editor.commit();
                Log.v(LOG_TAG, "Event id user just joined added to shared preferences.");
            }

            return resultStr;
        }

        @Override
        protected String doInBackground(Void... params) {



            // To join an event, you need the user id and the event id
            //int user_id = 0; // TODO: get user id from sharedprefs
            SharedPreferences settings = getActivity().getPreferences(0);

            int user_id = settings.getInt("user_id", 1);
            int event_id = Integer.parseInt(EventContract.EventEntry.getIdFromUri(mUri));



            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            URL url = null;

            // Will contain the raw JSON response as a string.
            String returnedJSON = null;

            // create connection to chillspot api, and open the connection
            try {

                // I don't know why, but it doesn't work unless .json is at the end
                // TODO: FIGURE OUT WHY THE FUCK THIS WON'T WORK WITHOUT .json AT END!!!
                url = new URL("http://evening-harbor-2864.herokuapp.com/events/" + event_id + "/users/" + user_id);

                Log.v("API", "url for join event: " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                // read input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    Log.e(LOG_TAG, "input stream null");
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // adds newline for debugging purposes
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream empty, don't parse
                    Log.e(LOG_TAG, "buffer length 0");
                    return null;
                }

                returnedJSON = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "ERROR: ", e);
                return null;
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

            // now to parse the JSON string
            String parsedJSON = null;

            try {
                parsedJSON = getReturnedJSON(returnedJSON);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON ERROR!" + e.toString());
                e.printStackTrace();
            }

            return parsedJSON;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) Log.v(LOG_TAG, "After request: " + result);
        }
    }
}