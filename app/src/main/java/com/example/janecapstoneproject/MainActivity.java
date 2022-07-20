package com.example.janecapstoneproject;
import static com.example.janecapstoneproject.Station.KEY_USERSSHAREDSTATIONS;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.maps.model.CircleOptions;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rey.material.app.ThemeManager;
import com.rey.material.app.ToolbarManager;
import com.rey.material.drawable.ThemeDrawable;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.Slider;
import com.squareup.picasso.Callback;

import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;

import org.parceler.Parcels;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import java.util.Vector;
import de.sfuhrm.radiobrowser4j.RadioBrowser;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, VolumeController.VolumeCallback, LocationController.LocationCallback, Station.StationCallback {
    private MapView mMapView;
    public com.rey.material.widget.FloatingActionButton addStationButton,editStationButton;
    public int REQUEST_CODE = 1001;
    public static final int PUBLIC_CIRCLE_RGB = Color.rgb(0,233,255);
    public static final int PRIVATE_CIRCLE_RGB = Color.rgb(255,0,233);
    public static final int CURRENT_CIRCLE_RGB = Color.rgb(0,255,22);
    public static final int PUBLIC_MARKER_COLOR = 0;
    public static final int PRIVATE_MARKER_COLOR = 1;
    public static final int CURRENT_MARKER_COLOR = 2;
    public static final int DEFAULT_ZOOM = 15;
    public static final int PUBLIC_TYPE = 0;
    public static final int PRIVATE_TYPE = 1;
    public static final double STATION_INTERACTION_RADIUS_METERS = 80;
    public static final double STATION_DETECTION_RADIUS_METERS = 300;
    public static final double STATION_INTERACTION_RADIUS_KILOMETERS = STATION_INTERACTION_RADIUS_METERS * .001;
    public static final double STATION_DETECTION_RADIUS_KILOMETERS = STATION_DETECTION_RADIUS_METERS * .001;
    public static final String KEY_GEOPOINT = "geopoint";
    public static final String TAG = "MainActivity";
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public ImageView musicIcon;
    public TextView nowPlayingText,stationNameText;
    LocationController locationController;
    VolumeController volumeController;
    GoogleMap globalMap;
    Drawable dayIcon, nightIcon;
    Slider volControl;
    MediaPlayerController mediaPlayerController;
    Station globalCurrentStation;
    Location globalLocation;
    private Toolbar mToolbar;
    private ToolbarManager mToolbarManager;
    Menu toolbarMenu;
    public static final int LIMIT_DEFAULT = 64;
    public static final int TIMEOUT_DEFAULT = 5000;
    RadioBrowser browser;
    String[] DNSlist;
    private boolean editHasBeenInitialized,bypassFavicon,bypassMap,bypassMedia;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editHasBeenInitialized = false;
        bypassMedia = false;
        bypassFavicon = false;
        bypassMap = false;
        initDrawables();
        initToolbar();
        initLocation();
        initMap(savedInstanceState);
        initAddStationButton(ParseUser.getCurrentUser(), this);
        initVolume();
        initMediaPlayer();
        initSlidingPanelElements();
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
        locationController.startLiveUpdates(this);
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


    //DRAWABLE CODE
    public void initDrawables() {
        dayIcon = AppCompatResources.getDrawable(this, R.drawable.daythemeicon_white);
        nightIcon = AppCompatResources.getDrawable(this, R.drawable.nightthemeicon_white);
    }

    //TOOLBAR CODE
    public void initToolbar() {
        mToolbar = findViewById(R.id.main_toolbar);
        mToolbarManager = new ToolbarManager(getDelegate(), mToolbar, R.id.tb_group_main, R.style.ToolbarRippleStyle, R.anim.abc_fade_in, R.anim.abc_fade_out);
        ViewUtil.setBackground(getWindow().getDecorView(), new ThemeDrawable(R.array.bg_window));
        ViewUtil.setBackground(mToolbar, new ThemeDrawable(R.array.bg_toolbar));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbarManager.createMenu(R.menu.menu_main);
        toolbarMenu = menu;
        retrieveCurrentTheme();
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //mToolbarManager.onPrepareMenu();
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tb_signout:
                logoutUser();
                break;
            case R.id.tb_theme:
                rotateTheme();
                break;
            case R.id.tb_question:
                launchInfoPopUp();
                break;
        }
        return true;
    }
    private void launchInfoPopUp() {
        View instructionsView = LayoutInflater.from(MainActivity.this).
                inflate(R.layout.instructions_item, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(instructionsView);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alertDialog.show();
    }
    private void updateThemeButtonIcon(int theme) {
        if (theme == 0) {
            toolbarMenu.getItem(1).setIcon(dayIcon);
        } else if (theme == 1) {
            toolbarMenu.getItem(1).setIcon(nightIcon);
        }
    }


    //SLIDING PANEL ELEMENTS
    public void initSlidingPanelElements(){
        initNowPlayingText();
        initStationNameText();
        initMusicIcon();
    }
    public void setSlidingPanelElements(Context context, String favicon,String name, String streamName){
        setMusicIcon(context, favicon,bypassFavicon);
        setStationNameText(name);
        setNowPlayingText(streamName,false);
    }
    //MUSIC ICON CODE
    private void initMusicIcon(){
        musicIcon = findViewById(R.id.musicIcon);
        musicIcon.setVisibility(View.INVISIBLE);
    }
    private String currentFavicon;
    private void setMusicIcon(Context context, String favicon, boolean bypass){
        boolean proceed = false;
        if (currentFavicon == null){
            proceed = true;
        }
        else if (currentFavicon.trim().isEmpty()) {
            proceed = true;
        }
        else if (bypass){
            proceed = true;
        }
        else if (favicon == null) {

        }
        else if (!favicon.equals(currentFavicon)) {
            proceed = true;
        }
        else{

        }
        if (proceed){
            if (favicon.isEmpty()){
                musicIcon.setVisibility(View.INVISIBLE);
            }else {
                Picasso.with(context).load(favicon).placeholder((R.drawable.ic_launcher_background)).error(R.drawable.ic_launcher_background).into(musicIcon, new Callback() {
                    @Override
                    public void onSuccess() {
                        musicIcon.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        Log.e(TAG, "Error loading favicon into musicIcon with Picasso");
                        musicIcon.setVisibility(View.INVISIBLE);
                    }
                });
            }
            bypassFavicon = false;
            currentFavicon = favicon;

        }
        else {
            //musicIcon.setVisibility(View.INVISIBLE);
        }
    }
    //STATION NAME TEXT CODE
    private void initStationNameText(){
        stationNameText = findViewById(R.id.stationNameText);
    }
    private String currentStationName;
    private void setStationNameText(String stationName){
        if (stationName != null && !stationName.trim().isEmpty() && !stationName.equals(currentStationName)) {
            String string;
            if (stationName.equals(getString(R.string.noStationFound))){
                string = getString(R.string.noStationFound);
            }
            else{
                string = stationName + ":";
            }
            stationNameText.setText(string);
            currentStationName = stationName;
        }
    }
    //NOW PLAYING TEXT CODE
    private void initNowPlayingText() {
        nowPlayingText = findViewById(R.id.nowPlayingText);
    }
    private String currentNowPlayingText;
    private void setNowPlayingText(String nowPlayingText,boolean bypass) {
        boolean proceed = false;
        if (currentNowPlayingText == null){
            proceed = true;
        }
        else if (currentNowPlayingText.trim().isEmpty()) {
            proceed = true;
        }
        else if (bypass){
            proceed = true;
        }
        else if (nowPlayingText == null) {

        }
        else if (!nowPlayingText.equals(currentNowPlayingText)) {
            proceed = true;
        }
        else{

        }
        if (proceed) {
            this.nowPlayingText.setText(nowPlayingText);
            currentNowPlayingText = nowPlayingText;
        }
    }
    //MEDIA CONTROLLER
    private void initMediaPlayer() {
        mediaPlayerController = new MediaPlayerController(this);
    }

    //VOLUME CONTROLLER
    private void initVolume() {
        volControl = (Slider) findViewById(R.id.volumebar);
        volumeController = new VolumeController(this, this);
        volControl.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                // TODO Auto-generated method stub
                volumeController.setVolume(newValue);
            }
        });
    }

    @Override
    public void onVolumeChanged(int volume) {
        volControl.setValue(volume, true);
    }

    @Override
    public void onMaxVolumeChanged(int maxVolume) {
        volControl.setValueRange(0, maxVolume, false);
    }

    //LOGOUT CODE
    private void logoutUser() {
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
        Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT);
        onStop();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    //LOCATION CONTROLLER
    private void initLocation() {
        locationController = new LocationController(this);
        locationController.registerCallback(this);
    }

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
        Log.d("MainActivity","onLocationResult");
        globalLocation = location;
        //maps, update stationRecycler
        if (globalMap != null) {
            if(bypassMap) {
                globalMap.setMyLocationEnabled(true);
                globalMap.getUiSettings().setMyLocationButtonEnabled(true);
                globalMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(),
                                location.getLongitude()), DEFAULT_ZOOM));
                bypassMap = false;
            }
            renderNearbyStations(MainActivity.this, location);
        }
    }
    @Override
    public void onRetrieveLocationResultAccompanyingBypass() {
        bypassMedia = true;
        bypassFavicon = true;
        bypassMap = true;
    }

    //MAP CODE (TODO: refactor through a controller if time permits)
    public void initMap(Bundle savedInstanceState) {
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
    }

    public void updateMapStyle(int themeNumber) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success;
            if (themeNumber == 0) {
                success = globalMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.map_day_theme_json));
            } else {
                success = globalMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.map_night_theme_json));
            }
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    //STATION CODE (TODO: refactor through a controller if time permits)
    public void initAddStationButton(ParseUser user, Context context) {
        addStationButton = findViewById(R.id.createButton);
        addStationButton.setVisibility(View.INVISIBLE);
        addStationButton.setOnClickListener(v -> {
            addStationButton.setLineMorphingState((addStationButton.getLineMorphingState() + 1) % 2, true);
            showAlertDialogForPoint(new LatLng(globalLocation.getLatitude(), globalLocation.getLongitude()),user, context);
        });
    }
    public void initEditStationButton(Station station, LatLng latlng, ParseUser user, Context context) {
        editStationButton = findViewById(R.id.editButton);
        editHasBeenInitialized = true;
        editStationButton.setIcon(getDrawable(R.drawable.ic_baseline_settings_24),false);
        editStationButton.setVisibility(View.INVISIBLE);
        editStationButton.setOnClickListener(v -> {
            editStationButton.setVisibility(View.INVISIBLE);
            showAlertDialogForEdit(0,station.getName(),latlng);
        });
    }

    Station nearestStation;
    float shortestDistance;
    private void renderNearbyStations(Context context, Location location) throws IOException {
        ParseUser user = ParseUser.getCurrentUser();
        // specify what type of data we want to query - Station.class
        ParseQuery<Station> query = ParseQuery.getQuery(Station.class);
        //query.setLimit(20);
        //query.whereWithinKilometers(KEY_GEOPOINT,new ParseGeoPoint(location.getLatitude(),location.getLongitude()), STATION_RADIUS_KILOMETERS, true);
        query.whereWithinKilometers(KEY_GEOPOINT, new ParseGeoPoint(location.getLatitude(), location.getLongitude()), STATION_DETECTION_RADIUS_KILOMETERS);
        // start an asynchronous call for posts
        nearestStation = null;
        shortestDistance = Integer.MAX_VALUE;
        query.findInBackground(new FindCallback<Station>() {
            @Override
            public void done(List<Station> stations, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting stationRecycler", e);
                    return;
                }
                for (Station station : stations) {
                    boolean includeStation = false;
                    if (station.isPublic()) {
                        includeStation = true;
                    } else {
                        String objId = station.getObjectId();
                        JSONArray array = user.getJSONArray(KEY_USERSSHAREDSTATIONS);
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                try {
                                    if (objId.equals(array.get(i))) {
                                        includeStation = true;
                                    }
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    if (includeStation) {
                        if ((globalCurrentStation == null || !station.getObjectId().equals(globalCurrentStation.getObjectId()))){
                            renderStation(station);
                        }
                        float[] result = new float[1];
                        android.location.Location.distanceBetween(location.getLatitude(), location.getLongitude(), station.getLatitude(), station.getLongitude(), result);
                        //might crash here check if it's null before but I think I've been doing that wrong
                        if (result[0] <= shortestDistance) {
                            shortestDistance = result[0];
                            nearestStation = station;
                        } else {
                            Log.e(TAG, "Result is null", e);
                        }
                    }
                }
                if (nearestStation != null) {
                    Log.d(TAG, "nearest station: " + nearestStation.getName());
                    if (shortestDistance <= STATION_INTERACTION_RADIUS_METERS) {
                        handleCaseValidNearestStation(context,nearestStation);
                        return;
                    } else {
                        Log.d(TAG, "Nearest station is too far!");
                        handleCaseNoNearbyStation(location);
                    }
                } else {
                    Log.e(TAG, "Nearest station is null", e);
                    handleCaseNoNearbyStation(location);
                }
            }
        });
    }

    public void renderStation(Station station) {
        Log.d(TAG, station.getName());
        if (station != null && station.getCoords() != null) {
            if (station.isPublic()) {
                station.setMarker(globalMap.addMarker(new MarkerOptions()
                        .position(station.getCoords())
                        .title(station.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastcyan))));
            } else {
                station.setMarker(globalMap.addMarker(new MarkerOptions()
                        .position(station.getCoords())
                        .title(station.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastpurple))));
            }
            addCircle(station, station.getCoords());
            return;
        }
        //TODO: fail
    }
    public void renderClosestStation(Context context, Station station){
        if (station.getMarker() == null){
            station.setMarker(globalMap.addMarker(new MarkerOptions()
                    .position(station.getCoords())
                    .title(station.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastgreen))));
        }
        else {
            station.setMarkerColor(CURRENT_MARKER_COLOR);
        }
        if(station.getCircle() == null){
            addCircle(station,station.getCoords());
        }

        setSlidingPanelElements(context, station.getFavicon(), station.getName(), station.getStreamName());
        if (!station.getStreamLink().isEmpty()) {
            mediaPlayerController.setURLAndPrepare(station.getStreamLink(),bypassMedia);
            bypassMedia = false;
        }
        globalCurrentStation = station;
    }
    public void handleCaseValidNearestStation(Context context, Station station) {
        boolean initEdit = false;
        if (globalCurrentStation != null && !station.getObjectId().equals(globalCurrentStation.getObjectId())) {
            renderStation(globalCurrentStation);
            initEdit = true;
        }
        addStationButton.setVisibility(View.INVISIBLE);
        renderClosestStation(context, station);
        if (initEdit){
            initEditStationButton(station,station.getCoords(),ParseUser.getCurrentUser(),context);
        }
        if (!editHasBeenInitialized){
            initEditStationButton(station,station.getCoords(),ParseUser.getCurrentUser(),context);
        }
        editStationButton.setVisibility(View.VISIBLE);
    }
    public void handleCaseNoNearbyStation(Location location) {
        addStationButton.setVisibility(View.VISIBLE);
        if (editHasBeenInitialized) {
            editStationButton.setVisibility(View.INVISIBLE);
        }

        if (globalCurrentStation != null){
            if (globalCurrentStation.getMarker() != null){
                if (globalCurrentStation.isPublic()) {
                    globalCurrentStation.setMarkerColor(0);
                }
                else{
                    globalCurrentStation.setMarkerColor(1);//(BitmapDescriptorFactory.fromResource(R.drawable.broadcastgreen));
                }
            }
        }
        mediaPlayerController.setURLAndPrepare(null,true);
        setStationNameText(getString(R.string.noStationFound));
        setNowPlayingText("",true);
        setMusicIcon(MainActivity.this,"",true);
        locationController.retrieveLocation(MainActivity.this);
    }
    public void addCircle(Station station, LatLng coords) {
        if (station.isPublic()) {
                station.setCircle(globalMap.addCircle(new CircleOptions()
                        .center(coords)
                        .radius(STATION_INTERACTION_RADIUS_METERS)
                        .strokeColor(PUBLIC_CIRCLE_RGB)
                        .fillColor(Color.TRANSPARENT).strokeWidth(5.0F)));
        }
        else {
                station.setCircle(globalMap.addCircle(new CircleOptions()
                        .center(coords)
                        .radius(STATION_INTERACTION_RADIUS_METERS)
                        .strokeColor(PRIVATE_CIRCLE_RGB)
                        .fillColor(Color.TRANSPARENT).strokeWidth(5.0F)));
            }
    }

    public void addStation(String name, int type, LatLng coords, ParseUser user, String streamLink, String streamName, String favicon, Context context) {
        try {
            saveStation(name, type, coords, user, streamLink, streamName, favicon, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void saveStation(String name, int type, LatLng coords, ParseUser user, String streamLink, String streamName, String favicon, Context context) throws JSONException {
        Station station = new Station();
        station.setName(name);
        station.setGeoPoint(coords);
        station.setStreamLink(streamLink);
        station.setStreamName(streamName);
        station.setFavicon(favicon);
        if (type == PRIVATE_TYPE) {
            station.setType(PRIVATE_TYPE);
            station.setUser(user);
            station.addUserToSharedList(user);
        } else {
            station.setType(PUBLIC_TYPE);
        }
        station.saveInBackground(e -> {
            if (type == PRIVATE_TYPE) {
                try {
                    station.addThisToUsersSharedList(user);
                    user.saveInBackground();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            if (e != null) {
                Log.e(TAG, "Error while saving", e);
                Toast.makeText(MainActivity.this, "Error while saving", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(TAG, "Station save was successful!!");
            locationController.retrieveLocation(context);
        });
    }
    private void addStationToAUsersSharedList(Station station, ParseUser user) {
        try {
            station.addThisToUsersSharedList(user);
            station.saveInBackground();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void addUserToAStationsSharedList(Station station, ParseUser user) {
        try {
            station.addUserToSharedList(user);
            user.saveInBackground();
            locationController.retrieveLocation(this);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void shareStationWithUser(Station station, ParseUser user) {
        addStationToAUsersSharedList(station, user);
        addUserToAStationsSharedList(station, user);
    }

    private void showAlertDialogForEdit(int isNew, String name, LatLng latLng) {
        View messageView2 = LayoutInflater.from(MainActivity.this).
                inflate(R.layout.confirm_edit_item, null);
        AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(this);
        alertDialogBuilder2.setView(messageView2);
        final AlertDialog alertDialog2 = alertDialogBuilder2.create();
        alertDialog2.setButton(DialogInterface.BUTTON_POSITIVE, "YES",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchBrowseStationsActivityForResult(isNew,name,latLng);
                    }
                });
        // Configure dialog button (Cancel)
        alertDialog2.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialog2.show();
    }

    private void showAlertDialogForPoint(LatLng latLng, ParseUser user, Context context) {
        // inflate message_item.xml view
        View messageView = LayoutInflater.from(MainActivity.this).
                inflate(R.layout.message_item, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(messageView);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Configure dialog button (OK)
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {/*
                        // Define color of marker icon
                        BitmapDescriptor defaultMarker =
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);*/
                        // Extract content from alert dialog
                        String name = ((EditText) alertDialog.findViewById(R.id.etTitle)).
                                getText().toString();
                        launchBrowseStationsActivityForResult(1,name,latLng);
                    }
                });
        // Configure dialog button (Cancel)
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        addStationButton.setLineMorphingState((addStationButton.getLineMorphingState() + 1) % 2, true);
                    }
                });
        // Display the dialog
        alertDialog.show();
    }
    private void launchBrowseStationsActivityForResult(int isNew, String name, LatLng latLng){
        Intent intent = new Intent(this, BrowseStationsActivity.class);
        intent.putExtra("stationName",name);
        intent.putExtra("new", isNew);
        intent.putExtra("latLng",Parcels.wrap(latLng));
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, String.valueOf(requestCode));
        Log.i(TAG, String.valueOf(resultCode));
        if ((requestCode == REQUEST_CODE )&& (resultCode == RESULT_OK)){
            String stationName = data.getStringExtra("stationName");
            String streamLink = data.getStringExtra("url");
            String streamName = data.getStringExtra("name");
            String favicon = data.getStringExtra("favicon");
            LatLng latLng = Parcels.unwrap(data.getParcelableExtra("latLng"));
            int isNew = data.getIntExtra("new",2);
            if (isNew == 1){
                addStation(stationName, PRIVATE_TYPE, latLng, ParseUser.getCurrentUser(), streamLink, streamName, favicon, MainActivity.this);
            }
            else if (isNew == 0){
                globalCurrentStation.updateStationWithNewRadioToParse(streamLink, streamName, favicon);

            }
            else{
                Log.e(TAG, "Retrieving new value != 0 or 1");
            }
        }
    }

    public void rotateTheme() {
        int theme = (ThemeManager.getInstance().getCurrentTheme() + 1) % ThemeManager.getInstance().getThemeCount();
        ThemeManager.getInstance().setCurrentTheme(theme);
        updateMapStyle(theme);
        updateThemeButtonIcon(theme);
    }

    public void retrieveCurrentTheme() {
        int theme = (ThemeManager.getInstance().getCurrentTheme()) % ThemeManager.getInstance().getThemeCount();
        ThemeManager.getInstance().setCurrentTheme(theme);
        updateMapStyle(theme);
        updateThemeButtonIcon(theme);
    }

    public int getCurrentTheme() {
        return (ThemeManager.getInstance().getCurrentTheme()) % ThemeManager.getInstance().getThemeCount();
    }
    // or just use de1
    void updateDnsListToPrepareBaseURL(){
        // start a thread and do the DNS request
        final AsyncTask<Void, Void, String[]> xxx = new AsyncTask<Void, Void, String[]>() {
            @Override
            protected String[] doInBackground(Void... params) {
                Vector<String> listResult = new Vector<String>();
                try {
                    // add all round robin servers one by one to select them separately
                    InetAddress[] list = InetAddress.getAllByName("all.api.radio-browser.info");
                    for (InetAddress item : list) {
                        listResult.add(item.getCanonicalHostName());
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                return listResult.toArray(new String[0]);
            }
            @Override
            protected void onPostExecute(String[] result) {
                // do something with the result
                super.onPostExecute(result);
                DNSlist = result;
                boolean contains = false;
                for (String string : result){
                    Log.d("browser",string);
                }
            }
        }.execute();
    }

    @Override
    public void onSaveInBackground() {
        locationController.retrieveLocation(this);
    }
}