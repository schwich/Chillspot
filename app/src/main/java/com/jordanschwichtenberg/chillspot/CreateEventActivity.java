package com.jordanschwichtenberg.chillspot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jordanschwichtenberg.chillspot.sync.ChillspotSyncAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class CreateEventActivity extends ActionBarActivity {

    private Button mSubmitEventButton;
    private EditText mCategoryEditText;
    private EditText mSubCategoryEditText;
    private EditText mNoteEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mSubmitEventButton = (Button) findViewById(R.id.submitEventButton);
        mSubmitEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: kick off createEvent task
                CreateEventTask apiCall = new CreateEventTask();
                apiCall.execute();
            }
        });

        mCategoryEditText = (EditText) findViewById(R.id.editText_category);
        mSubCategoryEditText = (EditText) findViewById(R.id.editText_subCategory);
        mNoteEditText = (EditText) findViewById(R.id.editText_note);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This will send a post request to the server that creates the event
     */
    public class CreateEventTask extends AsyncTask<Void, Void, String> {

        final String CHILLSPOT_STATUS_CODE = "status";
        final String CHILLSPOT_EVENT_ID = "event_id";

        private final String LOG_TAG = CreateEventTask.class.getSimpleName();

        private String getReturnedJSON(String jsonStr) throws JSONException {

            JSONObject data = new JSONObject(jsonStr);

            String statusCode = data.getString(CHILLSPOT_STATUS_CODE);

            int event_id = data.getInt(CHILLSPOT_EVENT_ID);

            Log.v(LOG_TAG, "Status code after event post: " + statusCode + ", and new event id: " + String.valueOf(event_id));

            String resultStr = "Status:\t" + statusCode +", Event_ID:\t" + event_id;

            //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
            sharedPreferences.edit()
                    .putInt("event_id_of_user", event_id)
                    .commit();



            return resultStr;
        }

        @Override
        protected String doInBackground(Void... params) {

            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
            int user_id = sharedPreferences.getInt("user_id", 1);

            // TODO: Build JSON string for creating an event
            String data = null;
            String category = "\"" + mCategoryEditText.getText().toString() + "\"";
            String subCategory = "\"" + mSubCategoryEditText.getText().toString() + "\"";
            String note = "\""  + mNoteEditText.getText().toString() + "\"";
            Double lat = null;
            Double lon = null;

            if (Utility.getLastLocation() != null) {
                Location loc = Utility.getLastLocation();
                lat = loc.getLatitude();
                lon = loc.getLongitude();
            }


            data = "{ \"event\": { " + "\"category\":"  + category + ", \"sub_category\": "
                   + subCategory + ", \"note\": " + note + ", \"latitude\": " + lat + ", \"longitude\": " + lon + " } }";

            Log.v(LOG_TAG, data);

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
                url = new URL("http://evening-harbor-2864.herokuapp.com/users/" + user_id + "/events");


                Log.v(LOG_TAG, "url for create event: " + url);

                // Connect
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                // Write
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(data);
                writer.close();
                os.close();

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

            // todo: sync?
            ChillspotSyncAdapter.syncImmediately(getApplicationContext());

            // todo: start intent from here?
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("tabPosition", MainActivity.YOUR_EVENT_TAB_INDEX);
            startActivity(intent);
        }
    }
}
