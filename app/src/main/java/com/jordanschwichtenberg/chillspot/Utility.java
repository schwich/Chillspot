package com.jordanschwichtenberg.chillspot;

import android.content.Context;
import android.location.Location;

/**
 * Created by Jordan on 4/11/2015.
 */
public class Utility {

    private static Location mLastLocation;

    public Utility() {

    }

    public static Location getLastLocation() {
        return mLastLocation;
    }

    public static void setLastLocation(Location lastLocation) {
        Utility.mLastLocation = lastLocation;
    }

    public static String formatDistance(Context context, double distance) {
        int distanceFormat = R.string.format_distance;

        return String.format(context.getString(distanceFormat), distance);
    }
}
