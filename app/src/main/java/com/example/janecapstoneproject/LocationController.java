package com.example.janecapstoneproject;

import static androidx.core.app.ActivityCompat.requestPermissions;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.ArrayList;

public class LocationController {
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Location globalLocation;
    private int REQUEST_CODE = 1001;
    private ArrayList<LocationController.LocationCallback> callbacks = new ArrayList<>();
    private com.google.android.gms.location.LocationCallback androidLocationCallback = new com.google.android.gms.location.LocationCallback() {
        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            Log.d("LocationController","LocationAvailability");
            super.onLocationAvailability(locationAvailability);
        }

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d("LocationController","onLocationResult");
            for(LocationCallback callback : callbacks) {
                try {
                    callback.onLocationResult(locationResult.getLastLocation());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    public LocationController(Context context){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void startLiveUpdates(Context context){
        locationRequest = LocationRequest.create().setInterval(10).setFastestInterval(1).setSmallestDisplacement(1)/*.setMaxWaitTime(1000)*/.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (checkForLocationPermission(context)) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    androidLocationCallback,
                    Looper.getMainLooper());
        }
    }

    public void stopLiveUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(androidLocationCallback);
    }

    public boolean checkForLocationPermission(Context context){
        //this.context = context;
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(Activity activity){
        requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }
    public void retrieveLocation(Context context) {
        try {
            if (!checkForLocationPermission(context)) {
                for (LocationCallback callback : callbacks){
                    callback.onPermissionsNeeded();
                }
            } else {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for(LocationCallback callback : callbacks) {
                            try {
                                callback.onRetrieveLocationResultAccompanyingBypass();
                                callback.onLocationResult(locationResult.getResult());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else{
                        //TODO: throw error, or try retrieving again
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public void registerCallback(LocationController.LocationCallback locationCallback){
        if (!callbacks.contains(locationCallback)){
            callbacks.add(locationCallback);
        }
    }

    public void unRegisterCallback(LocationController.LocationCallback locationCallback){
        if (callbacks.contains(locationCallback)){
            callbacks.remove(locationCallback);
        }
    }

    public Location getGlobalLocation(){
        return globalLocation;
    }
    public void setGlobalLocation(Location globalLocation) {
        this.globalLocation = globalLocation;
    }

    public interface LocationCallback{
        void onRetrieveLocationResultAccompanyingBypass();
        void onLocationResult(Location location) throws IOException;
        void onPermissionsNeeded();
    }
}