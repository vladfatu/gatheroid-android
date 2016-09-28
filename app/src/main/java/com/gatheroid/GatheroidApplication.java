package com.gatheroid;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by vlad on 28.09.2016.
 */

public class GatheroidApplication extends Application {

    private static final String TAG = "GatheroidApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application started");
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
