package com.example.angel.cloudy.sync;

import android.app.IntentService;
import android.content.Intent;

public class CloudySyncIntentService extends IntentService {

    public CloudySyncIntentService() {
        super("CloudySyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CloudySyncTask.syncWeather(this);
    }
}