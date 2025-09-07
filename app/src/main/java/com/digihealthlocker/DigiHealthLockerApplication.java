package com.digihealthlocker;

import android.app.Application;

import com.google.firebase.FirebaseApp;

import Utility.SharedPref;

public class DigiHealthLockerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        SharedPref.initialize(this);
    }
}