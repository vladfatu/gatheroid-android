package com.gatheroid;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final static String TAG = "MapsActivity";

    private GoogleMap mMap;

    private FirebaseAnalytics firebaseAnalytics;
    private DatabaseReference usersRef;
    private DatabaseReference directionRef;
    private MyChildEventListener childEventListener;
    private MyValueEventListener valueEventListener;
    private FirebaseAuth auth;
    private Map<String, Marker> markerMap;
    private Marker directionMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        markerMap = new HashMap<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle params = new Bundle();
        params.putString("activity_name", MapsActivity.class.getSimpleName());
        params.putString("method", "onCreate");
        firebaseAnalytics.logEvent("activity", params);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        usersRef = database.getReference("users");
        childEventListener = new MyChildEventListener();
        usersRef.addChildEventListener(childEventListener);

        directionRef = database.getReference("direction");
        valueEventListener = new MyValueEventListener();
        directionRef.addValueEventListener(valueEventListener);

        startService(new Intent(this, LocationService.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usersRef.removeEventListener(childEventListener);
        directionRef.removeEventListener(valueEventListener);
    }

    private void updateMarker(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.getKey().equals(auth.getCurrentUser().getUid())) {
            updateMarker(dataSnapshot.getKey(), String.class.cast(dataSnapshot.child("name").getValue()), Double.class.cast(dataSnapshot.child("location").child("latitude").getValue()), Double.class.cast(dataSnapshot.child("location").child("longitude").getValue()));
        }
    }

    private void updateMarker(String id, String name, double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        Marker marker = markerMap.get(id);
        if (marker == null) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            marker = mMap.addMarker(markerOptions);
            markerMap.put(id, marker);
        } else {
            marker.setPosition(latLng);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(new MyOnMapLongClickListener());
        mMap.setBuildingsEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(44.45, 26.09), 10);
        mMap.moveCamera(cameraUpdate);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopService(new Intent(this, LocationService.class));
    }

    private class MyValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                LatLng latLng = new LatLng(Double.class.cast(dataSnapshot.child("location").child("latitude").getValue()), Double.class.cast(dataSnapshot.child("location").child("longitude").getValue()));
                if (directionMarker == null) {
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Direction");
                    directionMarker = mMap.addMarker(markerOptions);
                } else {
                    directionMarker.setPosition(latLng);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class MyOnMapLongClickListener implements GoogleMap.OnMapLongClickListener {
        @Override
        public void onMapLongClick(LatLng latLng) {
            directionRef.child("location").child("latitude").setValue(latLng.latitude);
            directionRef.child("location").child("longitude").setValue(latLng.longitude);
        }
    }

    private class MyChildEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            updateMarker(dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            updateMarker(dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    }

}
