package com.gatheroid;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by vlad on 28.09.2016.
 */

public class LocationIntentService extends IntentService {

    public static final String TAG = "LocationIntentService";

    public LocationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Location update: ");
        Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        if(location !=null)
        {
            Log.d(TAG, "Location update: " + location.getLatitude() + ", " + location.getLongitude());
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users").child(auth.getCurrentUser().getUid());
            myRef.child("location").child("latitude").setValue(location.getLatitude());
            myRef.child("location").child("longitude").setValue(location.getLongitude());
        }
    }
}
