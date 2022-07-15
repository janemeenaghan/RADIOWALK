package com.example.janecapstoneproject;
import android.app.Application;
import com.parse.*;
import com.parse.Parse;
import okhttp3.*;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                    .setFontAttrId(R.attr.)
                    .build()
            );

        ParseObject.registerSubclass(Station.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());
    }
}