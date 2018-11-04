package com.example.nadus.pu_planner.OfflineCapabilities;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseOffline extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
