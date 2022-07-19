package com.example.janecapstoneproject;
import android.app.Application;
import com.parse.*;
import com.parse.Parse;
import com.rey.material.app.ThemeManager;
import okhttp3.*;
public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.init(this, 2, 0, null);
        ParseObject.registerSubclass(Station.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());
    }
}