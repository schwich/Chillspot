package com.jordanschwichtenberg.chillspot;

import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jordanschwichtenberg.chillspot.data.EventContract;

public class MapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

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

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Cursor mCursor = null;
    private MapView mView;
    ViewGroup layout;

    @Override
    public void onConnected(Bundle bundle) {

        Log.d("MAP", "does onConnected get called?");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            Utility.setLastLocation(location);
        }

        // update map position
        Location myLocation = Utility.getLastLocation();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 13);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        layout = (LinearLayout) inflater.inflate(R.layout.fragment_map, container, false);
        mView = (MapView) layout.findViewById(R.id.mapView);
        mView.onCreate(savedInstanceState);

        mMap = mView.getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        MapsInitializer.initialize(this.getActivity());

        // zoom camera to current location
        /*Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);*/

        // TODO: Have map zoom in on your current location
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.6916439, -122.474819), 13);
        mMap.animateCamera(cameraUpdate);

        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(EVENTS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = EventContract.EventEntry.COLUMN_DISTANCE + " ASC";
        Uri eventsUri = EventContract.EventEntry.CONTENT_URI;

        return new CursorLoader(getActivity(), eventsUri, EVENT_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;

        // update map markers
        updateMapMarkers();
    }

    private void updateMapMarkers() {

        Log.d("MAP", "Does updateMapMarkers get called?");

        while (mCursor.moveToNext()) {
            LatLng eventLocation = new LatLng(mCursor.getDouble(COL_EVENT_LATITUDE),
                    mCursor.getDouble(COL_EVENT_LONGITUDE));
            mMap.addMarker(new MarkerOptions()
                .position(eventLocation)
                .title("Event"));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mView.onResume();

        //setUpMapIfNeeded();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mView.onPause();
    }
}