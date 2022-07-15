package com.example.janecapstoneproject;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.model.CircleOptions;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import org.json.JSONException;
import java.io.IOException;
import java.util.List;


import de.sfuhrm.radiobrowser4j.FieldName;
import de.sfuhrm.radiobrowser4j.ListParameter;
import de.sfuhrm.radiobrowser4j.RadioBrowser;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, VolumeController.VolumeCallback, LocationController.LocationCallback {
    private MapView mMapView;
    public FloatingActionButton addStationButton, logout;
    public int REQUEST_CODE = 1001;
    public static final int DEFAULT_ZOOM = 20;
    public static final int PUBLIC_TYPE = 0;
    public static final int PRIVATE_TYPE = 1;
    public static final double STATION_RADIUS_METERS = 20;
    public static final double STATION_RADIUS_KILOMETERS = STATION_RADIUS_METERS * .001;
    public static final String KEY_GEOPOINT = "geopoint";
    public static final String TAG = "MainActivity";
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public TextView nowPlayingText;
    LocationController locationController;
    VolumeController volumeController;
    GoogleMap globalMap;
    SeekBar volControl;
    MediaPlayerController mediaPlayerController;
    Location globalLocation;
    Uri myUri;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLocation();
        initMap(savedInstanceState);
        initLogoutButton();
        initStationButton();
        initVolume();
        initMediaPlayer();
        initNowPlayingText();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initRadioBrowser();
        }
    }

    //LIFECYCLE EVENTS
    //TODO: add Location or station related info here?
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
        locationController.stopLiveUpdates();
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

    //RADIO BROWSER CODE
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void initRadioBrowser(){
        // 5000ms timeout, user agent is Demo agent/1.0
        RadioBrowser browser = new RadioBrowser(5000, "Demo agent/1.0");
        // print the first 64 stations in station name order
        browser.listStations(ListParameter.create().order(FieldName.NAME))
                .limit(64)
                .forEach(s -> Log.d("HHH",""+s.getName()+": "+ s.getUrl()));
    }

    //NOW PLAYING TEXT CODE
    public void initNowPlayingText(){
        nowPlayingText = findViewById(R.id.nowPlayingText);
    }
    public void setNowPlayingText(String text){
        nowPlayingText.setText(text);
    }

    //MEDIA CONTROLLER
    private void initMediaPlayer(){
        mediaPlayerController = new MediaPlayerController(this);
    }

    //VOLUME CONTROLLER
    private void initVolume(){
        volControl = (SeekBar)findViewById(R.id.volumebar);
        volumeController = new VolumeController(this, this);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                volumeController.setVolume(arg1);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    @Override
    public void onVolumeChanged(int volume) {
        volControl.setProgress(volume);
    }
    @Override
    public void onMaxVolumeChanged(int maxVolume) {
        volControl.setMax(maxVolume);
    }

    //LOGOUT CODE
    public void initLogoutButton(){
        logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(v -> {
            Log.i(TAG, "onClick logout button");
            logoutUser();
        });
    }
    private void logoutUser() {
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
        Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT);
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    //LOCATION CONTROLLER
    private void initLocation(){
        locationController = new LocationController(this);
        locationController.registerCallback(this);
    }
    //I think I no longer need this, commenting in case I change mind though
    /*@RequiresApi(api = Build.VERSION_CODES.M)
    private void requestLocation() {
        if (!locationController.checkForLocationPermission(this)) {
            locationController.requestPermission(this);
            return;
        }
        else{
            locationController.retrieveLocation();
        }
    }*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (locationController.checkForLocationPermission(this)) {
                        locationController.retrieveLocation(this);
                        return;
                    }
                }
            }
        }
        locationController.requestPermission(this);
    }
    @Override
    public void onPermissionsNeeded() {
        //TODO: spam user
        locationController.requestPermission(this);
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onLocationResult(Location location) throws IOException {
        globalLocation = location;
        //maps, update stations
        if (globalMap != null) {
            globalMap.setMyLocationEnabled(true);
            globalMap.getUiSettings().setMyLocationButtonEnabled(true);
            globalMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(),
                            location.getLongitude()), DEFAULT_ZOOM));
        }
        renderNearbyStations(location);
        //TODO: something about if there is a nearby station (playing its stream link, greying out button)
        //         //only visible when there is no nearby station - hide when not
        //
        //
    }

    //MAP CODE
    public void initMap(Bundle savedInstanceState){
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap map) {
        globalMap = map;
        locationController.retrieveLocation(this);
        locationController.startLiveUpdates(this);
        //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    //STATION CODE
    public void initStationButton(){
        addStationButton = findViewById(R.id.createButton);
        addStationButton.setVisibility(View.INVISIBLE);
        addStationButton.setOnClickListener(v -> {
            showAlertDialogForPoint();
        });
    }
    Station nearestStation;
    float shortestDistance;
    private void renderNearbyStations(Location location) throws IOException {
        // specify what type of data we want to query - Station.class
        ParseQuery<Station> query = ParseQuery.getQuery(Station.class);
        query.setLimit(20);
        //query.whereWithinKilometers(KEY_GEOPOINT,new ParseGeoPoint(location.getLatitude(),location.getLongitude()), STATION_RADIUS_KILOMETERS, true);
        query.whereWithinKilometers(KEY_GEOPOINT,new ParseGeoPoint(location.getLatitude(),location.getLongitude()), STATION_RADIUS_KILOMETERS);
        // start an asynchronous call for posts
        nearestStation = null;
        shortestDistance = Integer.MAX_VALUE;
        query.findInBackground(new FindCallback<Station>() {
            @Override
            public void done(List<Station> stations, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting stations", e);
                    return;
                }
                for (Station station : stations){
                    renderStation(station);
                    float[] result = new float[1];
                    android.location.Location.distanceBetween(location.getLatitude(),location.getLongitude(),station.getLatitude(),station.getLongitude(),result);
                    if (result != null) {
                        if (result[0] <= shortestDistance){
                            shortestDistance = result[0];
                            nearestStation = station;
                        }
                    }
                    else{
                        Log.e(TAG, "Result is null", e);
                    }
                }
                if (nearestStation!=null) {
                    Log.d(TAG, "nearest station: " + nearestStation.getName());
                    if (shortestDistance <= 20) {
                        handleValidNearestStation(nearestStation);
                        return;
                    }
                    else{
                        Log.d(TAG, "Nearest station is too far!");
                    }
                }
                else{
                    Log.e(TAG, "Nearest station is null", e);
                }
                handleNoNearbyStation(location);
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
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.threedredstation)));
            } else {
                globalMap.addMarker(new MarkerOptions()
                        .position(station.getCoords())
                        .title(station.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.threedblackstation)));
            }
            addCircle(station, station.getCoords());
            return;
        }
        //TODO: fail*/
    }
    public void handleValidNearestStation(Station station){
        addStationButton.setVisibility(View.INVISIBLE);
        setNowPlayingText("Now Playing: "+station.getName());
        if(!station.getStreamLink().isEmpty()) {
            mediaPlayerController.setURLAndPrepare(station.getStreamLink());
        }
    }
    public void handleNoNearbyStation(Location location){
        setNowPlayingText("No nearby station detected");
        addStationButton.setVisibility(View.VISIBLE);

        //TODO: other stuff probably
    }
    public void addCircle(Station station, LatLng coords){
        globalMap.addCircle(new CircleOptions()
                .center(coords)
                .radius(STATION_RADIUS_METERS)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));
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
        station.setGeoPoint(coords);
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
    private void showAlertDialogForPoint() {
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
                        double lat = globalLocation.getLatitude();
                        double lon = globalLocation.getLongitude();
                        //TODO: fix if statement if needed
                        if (!globalLocation.equals(null)) {
                            addStation(name, typeBoolean, new LatLng(globalLocation.getLatitude(), globalLocation.getLongitude()), ParseUser.getCurrentUser(), streamLink);
                        }
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