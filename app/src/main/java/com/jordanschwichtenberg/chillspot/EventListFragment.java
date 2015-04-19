package com.jordanschwichtenberg.chillspot;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jordanschwichtenberg.chillspot.data.EventContract;
import com.jordanschwichtenberg.chillspot.sync.ChillspotSyncAdapter;

/**
 * Created by Jordan on 3/18/2015.
 */
public class EventListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EVENTS_LOADER = 0;

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

    private static final String[] EVENT_COLUMNS = {
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

    //private ArrayAdapter<String> mEventListAdapter;

    private EventAdapter mEventAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    public EventListFragment() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: default sort order should be by distance
        //String sortOrder = EventContract.EventEntry.COLUMN_DISTANCE + " ASC";
        String sortOrder = null;
        Uri eventsUri = EventContract.EventEntry.CONTENT_URI;

        return new CursorLoader(getActivity(), eventsUri, EVENT_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mEventAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEventAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(EVENTS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eventlist, container, false);

        mEventAdapter = new EventAdapter(getActivity(), null, 0);

        mListView = (ListView) rootView.findViewById(R.id.listview_eventlist);
        mListView.setAdapter(mEventAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(EventContract.EventEntry.buildEventUri(cursor.getLong(COL_EVENT_ID)));
                }
            }
        });

        /*// kick off async task
        FetchEventsListTask eventTask = new FetchEventsListTask();
        eventTask.execute();*/

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.eventlistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            ChillspotSyncAdapter.syncImmediately(getActivity());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface Callback {
        public void onItemSelected(Uri eventUri);
    }

    /*public class FetchEventsListTask extends AsyncTask<Void, Void, String[]> {

        final String CHILLSPOT_EVENT_ID = "id";
        final String CHILLSPOT_EVENT_ADDRESS = "address";
        final String CHILLSPOT_EVENT_LATITUDE = "latitude";
        final String CHILLSPOT_EVENT_LONGITUDE = "longitude";
        private final String LOG_TAG = FetchEventsListTask.class.getSimpleName();

        private String[] getEventDataFromJSON(String jsonStr) throws JSONException {

            JSONArray eventsList = new JSONArray(jsonStr);

            String[] resultStrs = new String[eventsList.length()];
            for (int i = 0; i < eventsList.length(); i++) {
                String address;
                String latitude;
                String longitude;
                String id;

                JSONObject eventObject = eventsList.getJSONObject(i);
                id = eventObject.getString(CHILLSPOT_EVENT_ID);
                address = eventObject.getString(CHILLSPOT_EVENT_ADDRESS);
                latitude = eventObject.getString(CHILLSPOT_EVENT_LATITUDE);
                longitude = eventObject.getString(CHILLSPOT_EVENT_LONGITUDE);
                resultStrs[i] = "ID: " + id + "\nAddress: " + address;
            }

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            URL url = null;

            // Will contain the raw JSON response as a string.
            String eventsListJsonStr = null;

            // create connection to chillspot api, and open the connection
            try {
                url = new URL("http://evening-harbor-2864.herokuapp.com/events");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // read input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
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
                    return null;
                }

                eventsListJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "ERROR: ", e);
                Log.e(LOG_TAG, urlConnection.getErrorStream().toString());
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
            String[] parsedJSON = null;

            try {
                parsedJSON = getEventDataFromJSON(eventsListJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON ERROR!" + e.toString());
                e.printStackTrace();
            }

            return parsedJSON;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mEventListAdapter.clear();
                for (String str : result) {
                    mEventListAdapter.add(str);
                }
            }
        }
    }*/
}
