package com.gatheroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

/**
 * Created by vfatu on 28.09.2016.
 */

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        intent.getAction();

//        Location location = (Location) intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);
    }
}