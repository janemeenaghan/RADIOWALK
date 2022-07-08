package com.example.janecapstoneproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mMapView;
    public FloatingActionButton addStationButton, logout;
    public int REQUEST_CODE = 1001;
    public static final int DEFAULT_ZOOM = 20;
    public static final int PUBLIC_TYPE = 0;
    public static final int PRIVATE_TYPE = 1;
    public static final String TAG = "MainActivity";
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    MediaPlayer mediaPlayer;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location location;
    GoogleMap globalMap;
    Uri myUri;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        setupMap();
        renderAllStations();
        //only visible when there is no nearby station - hide when not
        addStationButton = findViewById(R.id.createButton);
        addStationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noStationisNearby()){
                    LatLng point = new LatLng(location.getLatitude(),location.getLongitude());
                    showAlertDialogForPoint(point);
                }
            }
        });
        // streaming m3u8 here
        try {
            setupMusic(Uri.parse("https://tunein.streamguys1.com/secure-cnn?DIST=TuneIn&TGT=TuneIn&maxServers=2&key=86b5d0be2518b55f18f3b4b8983b13e1f1d93f79fd8cfabbcad60c5db115fac1&partnertok=eyJhbGciOiJIUzI1NiIsImtpZCI6InR1bmVpbiIsInR5cCI6IkpXVCJ9.eyJ0cnVzdGVkX3BhcnRuZXIiOnRydWUsImlhdCI6MTY1NzE2Njk1NiwiaXNzIjoidGlzcnYifQ.dDZpnageeieHAKQPg0F3s4AcWr2XK-devjmobH5m9iI&aw_0_1st.playerid=ydvgH5BP&aw_0_1st.skey=1657166956&lat=39.0469&lon=-77.4903&aw_0_1st.premium=false&source=TuneIn&aw_0_1st.platform=tunein&aw_0_1st.genre_id=g3124&aw_0_1st.class=talk&aw_0_1st.ads_partner_alias=ydvgH5BP"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AudioManager leftAm = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = leftAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = leftAm.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volControl = (SeekBar)findViewById(R.id.volumebar);
        volControl.setMax(maxVolume);
        volControl.setProgress(curVolume);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                leftAm.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick logout button");
                logoutUser();
            }
        });
    }

    public void setupMap() {
        if (mMapView != null) {
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onMapReady(GoogleMap map) {
                    globalMap = map;
                    centerOnMyLocation();
                }
            });
        }
    }
    private void logoutUser() {
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null

        Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT);


        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        // finish() fixes the problem discussed with Naga about users going back to activities they aren't supposed to
        //finish();
    }

    public boolean noStationisNearby(){
        return true;
    }

    public void setupMusic(Uri myUri) throws IOException {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), myUri);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    public void renderAllStations() {
        // specify what type of data we want to query - Station.class
        ParseQuery<Station> query = ParseQuery.getQuery(Station.class);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Station>() {
            @Override
            public void done(List<Station> stations, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting stations", e);
                    return;
                }
                for (Station station : stations){
                    renderStation(station);
                }
            }
        });
    }
    public void renderStation(Station station){
        Log.d(TAG, station.getName());
        if (station != null && station.getCoords() != null) {
            if (station.isPublic()) {
                globalMap.addMarker(new MarkerOptions()
                        .position(station.getCoords())
                        .title(station.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tempmusicicon))
                        );
            } else {
                globalMap.addMarker(new MarkerOptions()
                        .position(station.getCoords())
                        .title(station.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_signout)));
            }
            return;
        }
        //TODO: fail*/
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void centerOnMyLocation() {
        if (!checkForLocationPermission()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        else{
            getDeviceLocationAndUpdateMap();
        }
    }

    private void getDeviceLocationAndUpdateMap() {
        CancellationToken whatDoesThisMean;
        Log.d(TAG, "top");
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (checkForLocationPermission()) {
                whatDoesThisMean = new CancellationToken() {
                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }
                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }
                };
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        globalMap.setMyLocationEnabled(true);
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            location = task.getResult();
                            if (location != null) {
                                globalMap.setMyLocationEnabled(true);
                                globalMap.getUiSettings().setMyLocationButtonEnabled(true);
                                globalMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(location.getLatitude(),
                                                location.getLongitude()), DEFAULT_ZOOM));
                            }
                            else{
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                //TODO: fail
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            //TODO: fail
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkForLocationPermission()) {
                        getDeviceLocationAndUpdateMap();
                    }
                }
            }
        }
        //TODO: pop ups keep asking for permission continuously or send user to settings
    }

    public boolean checkForLocationPermission(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void addStation(String name, boolean type, LatLng coords, ParseUser user, String streamLink){
        try {
            saveStation(name, type, coords, user, streamLink);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void saveStation(String name, boolean type, LatLng coords, ParseUser user, String streamLink) throws JSONException {
        Station station = new Station();
        station.setName(name);
        station.setCoords(coords);
        station.setStreamLink(streamLink);
        if (type){
            station.setType(PRIVATE_TYPE);
            station.setUser(user);
        }
        else{
            station.setType(PUBLIC_TYPE);
        }
        station.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(MainActivity.this, "Error while saving", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Station save was successful!!");
            }
        });
    }

    private void showAlertDialogForPoint(final LatLng point) {
        // inflate message_item.xml view
        View  messageView = LayoutInflater.from(MainActivity.this).
                inflate(R.layout.message_item, null);
        // Create alert dialog builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set message_item.xml to AlertDialog builder
        alertDialogBuilder.setView(messageView);
        // Create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Configure dialog button (OK)
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Define color of marker icon
                        BitmapDescriptor defaultMarker =
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                        // Extract content from alert dialog
                        String name = ((EditText) alertDialog.findViewById(R.id.etTitle)).
                                getText().toString();
                        String streamLink = ((EditText) alertDialog.findViewById(R.id.etStreamLink)).
                                getText().toString();
                        boolean typeBoolean = ((Switch)(alertDialog.findViewById(R.id.typeSwitch))).isChecked();
                        // Creates and adds marker to the map
                        addStation(name,typeBoolean,new LatLng(location.getLatitude(),location.getLongitude()),ParseUser.getCurrentUser(), streamLink);
                    }
                });
        // Configure dialog button (Cancel)
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                });
        // Display the dialog
        alertDialog.show();
    }

}