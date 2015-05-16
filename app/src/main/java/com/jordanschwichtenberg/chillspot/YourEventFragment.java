package com.jordanschwichtenberg.chillspot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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


public class YourEventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private View mYourEventView;
    private View mYourEventViewContainer;
    private View mRootView;
    private View mViewGroupContainer;

    private Button mCreateEventButton;
    private Button mLeaveEventButton;
    private Button mGetDirectionsButton;

    private TextView mSubCategoryView;
    private TextView mCategoryView;
    private TextView mAddressView;
    private TextView mDistanceView;
    private TextView mCreatedAtView;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mNoteView;

    public static boolean updateViewFlag = false;

    private static final int YOUR_EVENT_DETAIL_LOADER = 1;

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

    int mIdOfUserEvent = -1;
    private Uri mUri;

    private String mEventLat, mEventLon;


    public YourEventFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(YOUR_EVENT_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (updateViewFlag) {
            updateEventView();
            updateViewFlag = false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mIdOfUserEvent != -1) {
            mUri = EventContract.EventEntry.buildEventUri(mIdOfUserEvent);
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
            mEventLat = lat.toString();
            mLatitudeView.setText("Latitude: " + Double.toString(lat));

            Double lon = data.getDouble(COL_EVENT_LONGITUDE);
            mEventLon = lon.toString();
            mLongitudeView.setText("Longitude: " + Double.toString(lon));

            String note = data.getString(COL_EVENT_NOTE);
            mNoteView.setText("Note\n" + note);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_your_event, container, false);

        mRootView = rootView;
        mViewGroupContainer = container;

        // Allow this fragment to handle menu events.
        setHasOptionsMenu(true);


        mYourEventView = rootView.findViewById(R.id.your_event_view);
        mYourEventViewContainer = rootView.findViewById(R.id.your_event_view_container);

        mSubCategoryView = (TextView) rootView.findViewById(R.id.your_detail_subcategory_textview);
        mCategoryView = (TextView) rootView.findViewById(R.id.your_detail_category_textview);
        mAddressView = (TextView) rootView.findViewById(R.id.your_detail_address_textview);
        mDistanceView = (TextView) rootView.findViewById(R.id.your_detail_distance_textview);
        mCreatedAtView = (TextView) rootView.findViewById(R.id.your_detail_created_at_textview);
        mLatitudeView = (TextView) rootView.findViewById(R.id.your_detail_lat_textview);
        mLongitudeView = (TextView) rootView.findViewById(R.id.your_detail_lon_textview);
        mNoteView = (TextView) rootView.findViewById(R.id.your_detail_note_textview);

        mLeaveEventButton = (Button) rootView.findViewById(R.id.leaveEventButton);
        mLeaveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaveEventTask apiCall = new LeaveEventTask();
                apiCall.execute();
            }
        });

        mGetDirectionsButton = (Button) rootView.findViewById(R.id.getDirectionsButton);
        mGetDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send intent to google maps
                Uri gmIntentUri = Uri.parse("google.navigation:q=" + mEventLat  + "," + mEventLon + "&mode=w");
                Log.v("Directions", gmIntentUri.toString());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });

        mCreateEventButton = (Button) rootView.findViewById(R.id.createEventButton);
        mCreateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateEventActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mIdOfUserEvent = sharedPreferences.getInt("event_id_of_user", -1);
        Log.v("YourEventFragment", "in onCreateView--- Event id of user: " + String.valueOf(mIdOfUserEvent));

        if (mIdOfUserEvent != -1) {
            mCreateEventButton.setVisibility(View.GONE);

            mLeaveEventButton.setVisibility(View.VISIBLE);
            mGetDirectionsButton.setVisibility(View.VISIBLE);

            mYourEventView.setVisibility(View.VISIBLE);
        } else {
            Log.v("API", "User's event_id: " + String.valueOf(mIdOfUserEvent));

            mCreateEventButton.setVisibility(View.VISIBLE);
            mLeaveEventButton.setVisibility(View.GONE);
            mGetDirectionsButton.setVisibility(View.GONE);
            mYourEventView.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void updateEventView() {
        if (mIdOfUserEvent != -1) {
            mCreateEventButton.setVisibility(View.GONE);

            mLeaveEventButton.setVisibility(View.VISIBLE);
            mGetDirectionsButton.setVisibility(View.VISIBLE);

            mYourEventView.setVisibility(View.VISIBLE);
        } else {
            Log.v("API", "User's event_id: " + String.valueOf(mIdOfUserEvent));

            mCreateEventButton.setVisibility(View.VISIBLE);
            mLeaveEventButton.setVisibility(View.GONE);
            mGetDirectionsButton.setVisibility(View.GONE);
            mYourEventView.setVisibility(View.GONE);
        }

        ((Callback) getActivity())
                .refreshYourEventView();


    }

    public interface Callback {
        public void refreshYourEventView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_your_event, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh_your_event_view) {
            updateEventView();
            Log.v("WHAT", "refresh your event called");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * This will send a Delete request to the server that removes the user from the particular event
     */
    public class LeaveEventTask extends AsyncTask<Void, Void, String> {

        final String CHILLSPOT_STATUS_CODE = "status";
        private final String LOG_TAG = LeaveEventTask.class.getSimpleName();

        private String getReturnedJSON(String jsonStr) throws JSONException {

            JSONObject data = new JSONObject(jsonStr);

            String statusCode = data.getString(CHILLSPOT_STATUS_CODE);

            String resultStr = "Status:\t" + statusCode;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.edit().remove("event_id_of_user").commit();

            return resultStr;
        }

        @Override
        protected String doInBackground(Void... params) {

            // To join an event, you need the user id and the event id
            //int user_id = 0; // TODO: get user id from sharedprefs
            SharedPreferences settings = getActivity().getPreferences(0);

            int user_id = settings.getInt("user_id", 1);

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
                url = new URL("http://evening-harbor-2864.herokuapp.com/events/" + mIdOfUserEvent + "/users/" + 1);

                Log.v("API", "url for leave event: " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
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
            mIdOfUserEvent = -1;
            updateEventView();
        }
    }
}
