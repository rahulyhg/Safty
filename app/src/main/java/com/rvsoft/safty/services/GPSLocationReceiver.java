package com.rvsoft.safty.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rvsoft.safty.interfaces.ProviderReceiverListener;

public class GPSLocationReceiver extends BroadcastReceiver {
    public static ProviderReceiverListener receiverListener;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent!=null){
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")){
                if (receiverListener!=null){
                    receiverListener.onProviderChange();
                }
            }
        }
    }
}
