package com.jordanschwichtenberg.chillspot.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Jordan on 4/11/2015.
 */
public class ChillspotAuthenticatorService extends Service {

    private ChillspotAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new ChillspotAuthenticator(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     * @param intent passed in
     * @return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
