package com.jordanschwichtenberg.chillspot;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class CreateEventActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

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
    /*public class CreateEventTask extends AsyncTask<Void, Void, String> {

        final String CHILLSPOT_STATUS_CODE = "status";
        private final String LOG_TAG = CreateEventTask.class.getSimpleName();

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
            SharedPreferences settings = MainActivity.getPreferences(0);

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

                Log.v(LOG_TAG, "url for leave event: " + url);

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
        }
    }*/
}
