package com.rvsoft.safty.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.geniusforapp.fancydialog.FancyAlertDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.rvsoft.safty.R;
import com.rvsoft.safty.helper.SessionHelper;
import com.rvsoft.safty.interfaces.ProviderReceiverListener;
import com.rvsoft.safty.services.GPSLocationReceiver;

import static com.rvsoft.safty.helper.Constant.REQUESTS.ENABLE_GPS;

public abstract class BaseActivity extends AppCompatActivity implements ProviderReceiverListener{

    private GPSLocationReceiver gpsLocationReceiver;
    private IntentFilter gpsIntentFilter;
    private SessionHelper session;

    private Activity mActivity;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //region GPS BROADCAST INIT
        gpsIntentFilter = new IntentFilter();
        String PROVIDER_ACTION = "android.location.PROVIDERS_CHANGED";
        gpsIntentFilter.addAction(PROVIDER_ACTION);
        gpsLocationReceiver = new GPSLocationReceiver();
        //endregion

        session = new SessionHelper(this);
        mActivity = this;
    }

    @Override
    public void onProviderChange() {
        if (App.getGoogleClient().isConnected()){
            checkGPSEnable(App.getGoogleClient());
        }else {
            App.getGoogleClient(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    checkGPSEnable(App.getGoogleClient());
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
        }
    }

    public void checkGPSEnable(GoogleApiClient googleClient) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * 10);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleClient, builder.build());
        builder.setAlwaysShow(true);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //All location setting are satisfied
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(mActivity, ENABLE_GPS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==ENABLE_GPS){
            if (resultCode!=RESULT_OK){
                new FancyAlertDialog.Builder(mActivity)
                        .setimageResource(R.drawable.ic_navigation)
                        .setTextSubTitle("Please Enable GPS")
                        .setBody("This App Required GPS to be Enabled,\nPlease Tap 'OK' to Enable")
                        .setPositiveButtonText("OK")
                        .setNegativeButtonText("Cancel")
                        .setOnPositiveClicked(new FancyAlertDialog.OnPositiveClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                checkGPSEnable(App.getGoogleClient());
                            }
                        })
                        .setOnNegativeClicked(new FancyAlertDialog.OnNegativeClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                finish();
                            }
                        }).build().show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //region LIFE CYCLE

    @Override
    protected void onPause() {
        super.onPause();
        if (gpsLocationReceiver!=null){
            unregisterReceiver(gpsLocationReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gpsLocationReceiver,gpsIntentFilter);
        App.getInstance().setProviderListener(this);
    }

    //endregion
}
