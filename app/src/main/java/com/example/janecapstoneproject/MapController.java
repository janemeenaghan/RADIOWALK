package com.example.janecapstoneproject;
import static com.example.janecapstoneproject.MainActivity.STATION_DETECTION_RADIUS_METERS;
import static com.example.janecapstoneproject.StationController.DEFAULT_ZOOM;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
public class MapController {
    public static final String TAG = "MapController";
    public static final int PUBLIC_CIRCLE_RGB = Color.rgb(0, 233, 255);
    public static final int PRIVATE_CIRCLE_RGB = Color.rgb(255, 0, 233);
    public static final int CURRENT_CIRCLE_RGB = Color.rgb(0, 255, 22);
    public static final int PUBLIC_MARKER_COLOR = 0;
    public static final int PRIVATE_MARKER_COLOR = 1;
    public static final int CURRENT_MARKER_COLOR = 2;
    public static final int TILT_SETTING_ANGLED = 45;
    public static final int TILT_SETTING_STREET = 90;
    public static final int TILT_SETTING_GPS = 0;
    private GoogleMap map;
    private Context context;
    private boolean firstInstance;
    private ArrayList<MapController.MapCallback> callbacks = new ArrayList<>();
    public MapController(Context context, GoogleMap map) {
        firstInstance=true;
        this.context = context;
        this.map = map;
    }
    //GETTERS, SETTERS
    public GoogleMap getMap() {
        return map;
    }
    //MAP RENDERING
    @SuppressLint("MissingPermission")
    public void updateMapPositioning(Location location,boolean bypassChecks) throws IOException {
        if (bypassChecks) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.clear();
            if (firstInstance) {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()),DEFAULT_ZOOM,TILT_SETTING_ANGLED, 0)));
                firstInstance = false;
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(location.getLatitude(),
                                location.getLongitude())));
            }
            for(MapController.MapCallback callback : callbacks) {
                callback.turnOffBypass();
            }
        }
    }
    public void updateMapStyle(int themeNumber) {
        try {
            boolean success;
            if (themeNumber == 0) {
                success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                context, R.raw.map_day_theme_json));
            } else {
                success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                context, R.raw.map_night_theme_json));
            }
            if (!success) {
            }
        } catch (Resources.NotFoundException e) {
        }
    }
    public void clear(){
        map.clear();
    }
    public Marker addMarker(MarkerOptions markerOptions){
        return map.addMarker(markerOptions);
    }
    //CIRCLE RENDERING ON MAP
    public Circle addCircleByInt(Station station, int which) {
        if (which == 0) {
            return addCircle(station, station.getCoords(), PUBLIC_CIRCLE_RGB, STATION_DETECTION_RADIUS_METERS);
        }
        else if (which == 1){
            return addCircle(station, station.getCoords(), PRIVATE_CIRCLE_RGB, STATION_DETECTION_RADIUS_METERS);
        }
        else {
            return addCircle(station, station.getCoords(), CURRENT_CIRCLE_RGB, STATION_DETECTION_RADIUS_METERS);
        }
    }
    public Circle addCircle(Station station, LatLng coords, int strokeColor, double radiusMeters) {
        return station.setCircleAndRetrieve(map.addCircle(new CircleOptions()
                .center(coords)
                .radius(radiusMeters)
                .strokeColor(strokeColor)
                .fillColor(Color.TRANSPARENT).strokeWidth(5.0F)));
    }
    public void registerCallback(MapController.MapCallback mapCallback){
        if (!callbacks.contains(mapCallback)){
            callbacks.add(mapCallback);
        }
    }
    public void unRegisterCallback(MapController.MapCallback mapCallback){
        if (callbacks.contains(mapCallback)){
            callbacks.remove(mapCallback);
        }
    }
    public interface MapCallback{
        void turnOffBypass();
    }
}