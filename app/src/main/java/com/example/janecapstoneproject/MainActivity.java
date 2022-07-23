package com.example.janecapstoneproject;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;
import com.rey.material.app.ThemeManager;
import com.rey.material.app.ToolbarManager;
import com.rey.material.drawable.ThemeDrawable;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.Slider;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.parceler.Parcels;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, VolumeController.VolumeCallback, LocationController.LocationCallback, Station.StationCallback, StationController.StationControllerCallback, MediaPlayerController.MediaPlayerCallback, MapController.MapCallback{
    private boolean editButtonHasBeenInitialized, bypassFaviconChecks,bypassMapChecks;
    public int REQUEST_CODE = 1001;
    public static final int PUBLIC_TYPE = 0;
    public static final int PRIVATE_TYPE = 1;
    public static final double STATION_DETECTION_RADIUS_METERS = 200;
    public static final double STATION_DETECTION_RADIUS_KILOMETERS = STATION_DETECTION_RADIUS_METERS * .001;
    public static final String KEY_GEOPOINT = "geopoint";
    public static final String TAG = "MainActivity";
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private Toolbar mToolbar;
    private Menu toolbarMenu;
    private MapView mMapView;
    private ImageView musicIcon;
    private TextView nowPlayingText, stationNameText,minusText,plusText;
    private Drawable dayIcon, nightIcon, playIcon, pauseIcon;
    private Slider volControl;
    private ImageButton playPauseButton;
    private com.rey.material.widget.FloatingActionButton addStationButton, editStationButton;

    private MapController mapController;
    private LocationController locationController;
    private VolumeController volumeController;
    private MediaPlayerController mediaPlayerController;
    private StationController stationController;
    private ToolbarManager mToolbarManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editButtonHasBeenInitialized = false;
        bypassFaviconChecks = false;
        bypassMapChecks = false;
        initDrawables();
        initToolbar();
        initStation();
        initLocation();
        initMap(savedInstanceState);
        initAddStationButton(ParseUser.getCurrentUser(), this);
        initMediaPlayer();
        initVolume();
        initSlidingPanelElements();
    }

    //LIFECYCLE EVENTS
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
        mediaPlayerController.reset();
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
        volumeController.unRegisterCallback(this);
        mediaPlayerController.terminatePlayer();
        mediaPlayerController.unRegisterCallback(this);
        stationController.unRegisterCallback(this);
        locationController.unRegisterCallback(this);
        mapController.unRegisterCallback((MapController.MapCallback) this);

        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    //LINKING AND LOGOUT CODE
    private void linkUserToFB(){
        Collection<String> permissions = Arrays.asList("public_profile", "email");
        if (!ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
            ParseFacebookUtils.linkWithReadPermissionsInBackground(ParseUser.getCurrentUser(), this, permissions, ex -> {
                if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
                    Toast.makeText(this, "Woohoo, user logged in with Facebook.", Toast.LENGTH_LONG).show();
                    Log.d("FacebookLoginExample", "Woohoo, user logged in with Facebook!");
                }
            });
        } else {
            Toast.makeText(this, "You have already linked your account with Facebook.", Toast.LENGTH_LONG).show();
        }
    }
    private void logoutUser() {
        LoginManager.getInstance().logOut();
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser();
        Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT);
        onStop();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    //DRAWABLE CODE
    private void initDrawables() {
        dayIcon = AppCompatResources.getDrawable(this, R.drawable.daythemeicon_white);
        nightIcon = AppCompatResources.getDrawable(this, R.drawable.nightthemeicon_white);
        playIcon = AppCompatResources.getDrawable(this, R.drawable.ic_media_play);
        pauseIcon = AppCompatResources.getDrawable(this, R.drawable.ic_media_pause);
    }

    //TOOLBAR CODE
    private void initToolbar() {
        mToolbar = findViewById(R.id.main_toolbar);
        mToolbarManager = new ToolbarManager(getDelegate(), mToolbar, R.id.tb_group_main, R.style.ToolbarRippleStyle, R.anim.abc_fade_in, R.anim.abc_fade_out);
        ViewUtil.setBackground(getWindow().getDecorView(), new ThemeDrawable(R.array.bg_window));
        ViewUtil.setBackground(mToolbar, new ThemeDrawable(R.array.bg_toolbar));
    }
    private void updateThemeButtonIcon(int theme) {
        if (theme == 0) {
            toolbarMenu.getItem(2).setIcon(dayIcon);
        } else if (theme == 1) {
            toolbarMenu.getItem(2).setIcon(nightIcon);
        }
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
            case R.id.tb_fb:
                linkUserToFB();
                break;
        }
        return true;
    }

    //QUERYING FILTER UI CODE
    public void onRadioButtonClicked(@NonNull View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.radio_public:
                stationController.setPublicPrivateSelection(0);
                break;
            case R.id.radio_private:
                stationController.setPublicPrivateSelection(1);
                break;
            case R.id.radio_both:
                stationController.setPublicPrivateSelection(2);
                break;
        }
        locationController.retrieveLocation(MainActivity.this);
    }



    private void rotateTheme () {
        int theme = (ThemeManager.getInstance().getCurrentTheme() + 1) % ThemeManager.getInstance().getThemeCount();
        ThemeManager.getInstance().setCurrentTheme(theme);
        mapController.updateMapStyle(theme);
        updateThemeButtonIcon(theme);
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

    //THEME MANAGER
    private void retrieveCurrentTheme () {
        int theme = (ThemeManager.getInstance().getCurrentTheme()) % ThemeManager.getInstance().getThemeCount();
        ThemeManager.getInstance().setCurrentTheme(theme);
        mapController.updateMapStyle(theme);
        updateThemeButtonIcon(theme);
    }
    private int getCurrentTheme () {
        return (ThemeManager.getInstance().getCurrentTheme()) % ThemeManager.getInstance().getThemeCount();
    }

    //SLIDING PANEL ELEMENTS
    private void initSlidingPanelElements() {
        initNowPlayingText();
        initStationNameText();
        initMusicIcon();
    }
    private void setSlidingPanelElements(Context context, String favicon, String name, String streamName) {
        setMusicIcon(context, favicon, bypassFaviconChecks);
        setStationNameText(name);
        setNowPlayingText(streamName, false);
    }
    //MUSIC ICON CODE
    private void initMusicIcon() {
        musicIcon = findViewById(R.id.musicIcon);
        musicIcon.setVisibility(View.INVISIBLE);
    }
    private String currentFavicon;
    private void setMusicIcon(Context context, String favicon, boolean bypass) {
        boolean proceed = false;
        if (currentFavicon == null) {
            proceed = true;
        } else if (currentFavicon.trim().isEmpty()) {
            proceed = true;
        } else if (bypass) {
            proceed = true;
        } else if (favicon == null) {

        } else if (!favicon.equals(currentFavicon)) {
            proceed = true;
        } else {

        }
        if (proceed) {
            if (favicon.isEmpty()) {
                musicIcon.setVisibility(View.INVISIBLE);
            } else {
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
            bypassFaviconChecks = false;
            currentFavicon = favicon;

        } else {
        }
    }
    //STATION NAME TEXT CODE
    private void initStationNameText() {
        stationNameText = findViewById(R.id.stationNameText);
    }
    private String currentStationName;
    private void setStationNameText(String stationName) {
        if (stationName != null && !stationName.trim().isEmpty() && !stationName.equals(currentStationName)) {
            String string;
            if (stationName.equals(getString(R.string.noStationFound))) {
                string = getString(R.string.noStationFound);
            } else {
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
    private void setNowPlayingText(String nowPlayingText, boolean bypass) {
        boolean proceed = false;
        if (currentNowPlayingText == null) {
            proceed = true;
        } else if (currentNowPlayingText.trim().isEmpty()) {
            proceed = true;
        } else if (bypass) {
            proceed = true;
        } else if (nowPlayingText == null) {

        } else if (!nowPlayingText.equals(currentNowPlayingText)) {
            proceed = true;
        } else {

        }
        if (proceed) {
            this.nowPlayingText.setText(nowPlayingText);
            currentNowPlayingText = nowPlayingText;
        }
    }

    //MEDIA CONTROLLER
    private void initMediaPlayer() {
        mediaPlayerController = new MediaPlayerController(this);
        mediaPlayerController.registerCallback(this);
        playPauseButton = findViewById(R.id.playPauseButton);
        playPauseButton.setVisibility(View.INVISIBLE);
        playPauseButton.setOnClickListener(v -> {
            if(mediaPlayerController.isPlaying()){
                mediaPlayerController.pause();
            }
            else{
                mediaPlayerController.startPlaying();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onPlayingChanged(int playingState) {
        //isPlaying
        if (playingState == 0) {
            playPauseButton.setImageDrawable(pauseIcon);
            Log.e(TAG,"0");
            playPauseButton.setVisibility(View.VISIBLE);
            musicIcon.setVisibility(View.VISIBLE);
        }
        //isPaused
        else if (playingState == 1) {
            playPauseButton.setImageDrawable(playIcon);
            playPauseButton.setVisibility(View.VISIBLE);
            musicIcon.setVisibility(View.VISIBLE);
            Log.e(TAG,"1");
        }
        //isStopped
        else{
            playPauseButton.setVisibility(View.INVISIBLE);
            musicIcon.setVisibility(View.INVISIBLE);
            Log.e(TAG,"else");
        }
    }
    @Override
    public void onMediaPlayerError() {}


    //VOLUME CONTROLLER
    private void initVolume() {
        plusText = findViewById(R.id.plusText);
        plusText.setOnClickListener(v -> {
            volumeController.raiseVolume();
        });
        minusText = findViewById(R.id.minusText);
        minusText.setOnClickListener(v -> {
            volumeController.lowerVolume();
        });
        volumeController = new VolumeController(this);
        volumeController.registerCallback(this);
        volControl = findViewById(R.id.volumebar);
        volControl.setValueRange(0,volumeController.getMaxVolume(),false);
        volControl.setValue(volumeController.getVolume(),false);
        volControl.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                volumeController.setVolume(newValue);
            }
        });

    }
    @Override
    public void onVolumeChanged(int volume) {
        volControl.setValue(volume, false);
    }
    @Override
    public void onMaxVolumeChanged(int maxVolume) {
        volControl.setValueRange(0, maxVolume, false);
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
        locationController.requestPermission(this);
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onLocationResult(Location location) throws IOException {
        Log.d("MainActivity", "onLocationResult");
        locationController.setGlobalLocation(location);
        if (mapController.getMap() != null) {
            mapController.updateMapPositioning(location,bypassMapChecks);
            stationController.renderNearbyStations(ParseUser.getCurrentUser(),MainActivity.this,location,STATION_DETECTION_RADIUS_KILOMETERS);
        }
    }
    @Override
    public void onRetrieveLocationResultAccompanyingBypass() {
        bypassMapChecks = true;
    }

    //STATION CONTROLLER
    private void initStation(){
        stationController = new StationController();
        stationController.registerCallback(this);
    }

    //MAP CONTROLLER
    private void initMap(Bundle savedInstanceState) {
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
        mapController = new MapController(this, map);
        mapController.registerCallback(this);
        locationController.retrieveLocation(this);
    }
    @Override
    public void turnOffBypass() {
        bypassMapChecks = false;
    }
    //STATION BUTTONS CODE
    private void initAddStationButton(ParseUser user, Context context) {
        addStationButton = findViewById(R.id.createButton);
        addStationButton.setVisibility(View.INVISIBLE);
        addStationButton.setOnClickListener(v -> {
            addStationButton.setLineMorphingState((addStationButton.getLineMorphingState() + 1) % 2, true);
            Location loc = locationController.getGlobalLocation();
            showAlertDialogForPoint(new LatLng(loc.getLatitude(), loc.getLongitude()), user, context);
        });
    }
    private void initEditStationButton(Station station, LatLng latlng, ParseUser user, Context context) {
        editStationButton = findViewById(R.id.editButton);
        editButtonHasBeenInitialized = true;
        editStationButton.setIcon(getDrawable(R.drawable.ic_baseline_settings_24), false);
        editStationButton.setVisibility(View.INVISIBLE);
        editStationButton.setOnClickListener(v -> {
            editStationButton.setVisibility(View.INVISIBLE);
            showAlertDialogForEdit(0, station.getName(), latlng);
        });
    }
    private void showAlertDialogForEdit ( int isNew, String name, LatLng latLng){
        View messageView2 = LayoutInflater.from(MainActivity.this).
                inflate(R.layout.confirm_edit_item, null);
        AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(this);
        alertDialogBuilder2.setView(messageView2);
        final AlertDialog alertDialog2 = alertDialogBuilder2.create();
        alertDialog2.setButton(DialogInterface.BUTTON_POSITIVE, "YES",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchBrowseStationsActivityForResult(isNew, name, latLng);
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

    private void showAlertDialogForPoint (LatLng latLng, ParseUser user, Context context){
        View messageView = LayoutInflater.from(MainActivity.this).inflate(R.layout.message_item, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(messageView);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = ((EditText) alertDialog.findViewById(R.id.etTitle)).
                                getText().toString();
                        launchBrowseStationsActivityForResult(1, name, latLng);
                        addStationButton.setLineMorphingState((addStationButton.getLineMorphingState() + 1) % 2, true);
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        addStationButton.setLineMorphingState((addStationButton.getLineMorphingState() + 1) % 2, true);
                    }
                });
        alertDialog.show();
    }
    private void launchBrowseStationsActivityForResult ( int isNew, String name, LatLng latLng){
        Intent intent = new Intent(this, BrowseStationsActivity.class);
        intent.putExtra("stationName", name);
        intent.putExtra("new", isNew);
        intent.putExtra("latLng", Parcels.wrap(latLng));
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, String.valueOf(requestCode));
        Log.i(TAG, String.valueOf(resultCode));
        if ((requestCode == REQUEST_CODE) && (resultCode == RESULT_OK)) {
            String stationName = data.getStringExtra("stationName");
            String streamLink = data.getStringExtra("url");
            String streamName = data.getStringExtra("name");
            String favicon = data.getStringExtra("favicon");
            LatLng latLng = Parcels.unwrap(data.getParcelableExtra("latLng"));
            int isNew = data.getIntExtra("new", 2);
            if (isNew == 1) {
                stationController.addStation(stationName, PRIVATE_TYPE, latLng, ParseUser.getCurrentUser(), streamLink, streamName, favicon, MainActivity.this);
            } else if (isNew == 0) {
                //Potential hazard
                stationController.getGlobalCurrentStation().updateStationWithNewRadioToParse(streamLink, streamName, favicon);

            } else {
                Log.e(TAG, "Retrieving new value != 0 or 1");
            }
        }
    }

    //STATIONCONTROLLER CALLBACKS
    @Override
    public void onSaveInBackground () {
        locationController.retrieveLocation(this);
    }
    @Override
    public void onCaseNoNearbyStation (Location location){
        addStationButton.setVisibility(View.VISIBLE);
        if (editButtonHasBeenInitialized) {
            editStationButton.setVisibility(View.INVISIBLE);
        }
        if (stationController.globalCurrentStationExists()) {
            if (stationController.getGlobalCurrentStation().getMarker() != null) {
                if (stationController.getGlobalCurrentStation().isPublic()) {
                    stationController.setGlobalCurrentStationMarkerColor(0);
                } else {
                    stationController.setGlobalCurrentStationMarkerColor(1);
                }
            }
        }
        mediaPlayerController.reset();//.setURLAndPrepare(null, true);
        setStationNameText(getString(R.string.noStationFound));
        setNowPlayingText("", true);
        setMusicIcon(MainActivity.this, "", true);
        //locationController.retrieveLocation(MainActivity.this);
    }
    @Override
    public void onCaseValidNearestStation (Location location, Station newNearestStation, boolean needToDeselectCurrentStation){
        boolean editButtonNeedsToBeRefreshed = false;
        if (needToDeselectCurrentStation) {
            mapController.renderStation(stationController.getGlobalCurrentStation());
            editButtonNeedsToBeRefreshed = true;
        }
        addStationButton.setVisibility(View.INVISIBLE);
        stationController.renderClosestStation(MainActivity.this, newNearestStation);
        if (editButtonNeedsToBeRefreshed || !editButtonHasBeenInitialized) {
            initEditStationButton(newNearestStation, newNearestStation.getCoords(), ParseUser.getCurrentUser(), MainActivity.this);
        }
        editStationButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveStation(Station station, ParseUser user) throws JSONException {
        station.addThisToUsersSharedList(user);
        user.saveInBackground();
        locationController.retrieveLocation(MainActivity.this);
    }

    public Marker onRequestMarkerAddedToClosestStation(Station closestStation) {
        return mapController.addMarker(new MarkerOptions()
                .position(closestStation.getCoords())
                .title(closestStation.getName())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastgreen)));
    }
    public Circle onRequestCircleAddedToClosestStation(Station closestStation) {
        return mapController.addCircleConventional(closestStation,closestStation.getCoords());
    }
    @Override
    public void updateUIToRenderClosestStation(Station closestStation) {
        setSlidingPanelElements(MainActivity.this, closestStation.getFavicon(), closestStation.getName(), closestStation.getStreamName());
        if (!closestStation.getStreamLink().isEmpty()) {
            mediaPlayerController.setURLAndPrepare(closestStation.getStreamLink());
        }
        stationController.setGlobalCurrentStation(closestStation);
    }

    public void renderAStationToMap(Station station) {
        mapController.renderStation(station);
    }
}