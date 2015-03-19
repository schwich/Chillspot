package com.jordanschwichtenberg.chillspot;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Jordan on 3/18/2015.
 */
public class EventListFragment extends Fragment {

    private ArrayAdapter<String> mEventListAdapter;

    public EventListFragment() {

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

        ArrayList<String> temp = new ArrayList<>();

        mEventListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_event,
                R.id.list_item_event_textview, temp);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_eventlist);
        listView.setAdapter(mEventListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String eventIDStr = mEventListAdapter.getItem(position);
                String eventID = eventIDStr.split(" ")[1];
                eventID = eventID.split("\n")[0];
                eventID = eventID.replaceAll("\\s+","");

                Log.v("HELLO", "EVENT ID SENT TO FRAGMENT: " + eventID);

                Intent intent = new Intent(getActivity(), EventDetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, eventID);
                startActivity(intent);
            }
        });

        // kick off async task
        FetchEventsListTask eventTask = new FetchEventsListTask();
        eventTask.execute();

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
            // kick off async task
            FetchEventsListTask eventTask = new FetchEventsListTask();
            eventTask.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchEventsListTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchEventsListTask.class.getSimpleName();

        final String CHILLSPOT_EVENT_ID = "id";
        final String CHILLSPOT_EVENT_ADDRESS = "address";
        final String CHILLSPOT_EVENT_LATITUDE = "latitude";
        final String CHILLSPOT_EVENT_LONGITUDE = "longitude";

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
    }
}
