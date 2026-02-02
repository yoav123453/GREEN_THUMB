package com.yoav_s.repository.BASE.DB;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseInstance {
    private static volatile FirebaseInstance _instance = null;
    public static FirebaseApp app;

    private FirebaseInstance(Context context) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId("greenthumb-50e66")
                .setApplicationId("greenthumb-50e66")
                .setApiKey("AIzaSyBi0bQdM5QdkvXN3Mgw8vUXlETfLFtxBbc")
                .setStorageBucket("greenthumb-50e66.firebasestorage.app")
                .build();

        app = FirebaseApp.initializeApp(context, options);
    }

    public static FirebaseInstance instance(Context context) {
        if (_instance == null) {  // 1st check
            synchronized (FirebaseInstance.class) {
                if (_instance == null){ // 2nd check
                    _instance = new FirebaseInstance(context);
                }
            }
        }

        return _instance;
    }
}
