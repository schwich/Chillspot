package com.jordanschwichtenberg.chillspot.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Jordan on 4/11/2015.
 */
public class ChillspotSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static ChillspotSyncAdapter sChillspotSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (sChillspotSyncAdapter == null) {
                sChillspotSyncAdapter = new ChillspotSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sChillspotSyncAdapter.getSyncAdapterBinder();
    }
}
