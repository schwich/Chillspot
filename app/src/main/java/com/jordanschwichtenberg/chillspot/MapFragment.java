package com.jordanschwichtenberg.chillspot;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;

public class MapFragment extends SupportMapFragment {

    private GoogleMap mMap;
    private MapView mView;
    ViewGroup layout;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        layout = (LinearLayout) inflater.inflate(R.layout.fragment_map, container, false);
        mView = (MapView) layout.findViewById(R.id.mapView);
        mView.onCreate(savedInstanceState);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        mView.onResume();

        setUpMapIfNeeded();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private void setUpMapIfNeeded() {
        // check if map is already created
        if (mMap == null) {
            SupportMapFragment.newInstance();
            mMap = mView.getMap();

            // check if map was obtained successfully
            if (mMap != null) {
                setUpMap();
            }

            setUpMap();
        } else {

            setUpMap();
        }
    }

    private void setUpMap() {
        mMap = mView.getMap();
        mMap.setMyLocationEnabled(true);
    }
}