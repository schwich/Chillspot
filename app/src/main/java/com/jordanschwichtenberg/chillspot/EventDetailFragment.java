package com.jordanschwichtenberg.chillspot;

/**
 * Created by Jordan on 3/18/2015.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EventDetailFragment extends Fragment {

    private String mEventID;
    private TextView mEventDetailTextView;

    public EventDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mEventID = intent.getStringExtra(Intent.EXTRA_TEXT);
            // strip whitespace and non-printable characters
            mEventID = mEventID.replaceAll("\\s+", "");
            //((TextView) rootView.findViewById(R.id.event_detail_text)).setText(mEventID);
        }

        mEventDetailTextView = (TextView) rootView.findViewById(R.id.event_detail_text);

        // kick off async task
        FetchEventDetailTask eventTask = new FetchEventDetailTask();
        eventTask.execute();

        return rootView;
    }

    public class FetchEventDetailTask extends AsyncTask<Void, Void, String> {

        final String CHILLSPOT_EVENT_ADDRESS = "address";
        final String CHILLSPOT_EVENT_LATITUDE = "latitude";
        final String CHILLSPOT_EVENT_LONGITUDE = "longitude";
        final String CHILLSPOT_EVENT_STARTED_AT = "event_started";
        private final String LOG_TAG = FetchEventDetailTask.class.getSimpleName();

        private String getEventDataFromJSON(String jsonStr) throws JSONException {

            JSONObject eventJsonData = new JSONObject(jsonStr);

            String address = eventJsonData.getString(CHILLSPOT_EVENT_ADDRESS);
            String latitude = eventJsonData.getString(CHILLSPOT_EVENT_LATITUDE);
            String longitude = eventJsonData.getString(CHILLSPOT_EVENT_LONGITUDE);
            String event_started_at = eventJsonData.getString(CHILLSPOT_EVENT_STARTED_AT);

            String resultStr = "Address:\n\t" + address + "\n\nLatitude:\n\t" + latitude
                    + "\n\nLongitude:\n\t" + longitude + "\n\nEvent started at:\n\t" + event_started_at;

            return resultStr;
        }

        @Override
        protected String doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            URL url = null;

            // Will contain the raw JSON response as a string.
            String eventDetailJsonStr = null;

            // create connection to chillspot api, and open the connection
            try {

                // I don't know why, but it doesn't work unless .json is at the end
                // TODO: FIGURE OUT WHY THE FUCK THIS WON'T WORK WITHOUT .json AT END!!!
                url = new URL("http://evening-harbor-2864.herokuapp.com/events/" + mEventID + ".json");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

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

                eventDetailJsonStr = buffer.toString();

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
                parsedJSON = getEventDataFromJSON(eventDetailJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON ERROR!" + e.toString());
                e.printStackTrace();
            }

            return parsedJSON;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                mEventDetailTextView.setText(result);
            }
        }
    }
}