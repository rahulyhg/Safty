package com.rvsoft.safty.app;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.rvsoft.safty.interfaces.ProviderReceiverListener;
import com.rvsoft.safty.services.GPSLocationReceiver;

public class App extends Application {
    public static App mInstance;
    public static GoogleApiClient mGoogleApiClient;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        mGoogleApiClient = new GoogleApiClient.Builder(mInstance.getApplicationContext())
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        FirebaseApp.initializeApp(this);
    }

    public static synchronized App getInstance(){
        return mInstance;
    }

    public static GoogleApiClient getGoogleClient(GoogleApiClient.ConnectionCallbacks connectionCallback){
        mGoogleApiClient.registerConnectionCallbacks(connectionCallback);
        return mGoogleApiClient;
    }

    public static GoogleApiClient getGoogleClient(){
        return mGoogleApiClient;
    }

    public void setProviderListener(ProviderReceiverListener listener){
        GPSLocationReceiver.receiverListener = listener;
    }
}
