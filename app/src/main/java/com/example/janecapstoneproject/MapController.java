package com.example.janecapstoneproject;

import static com.example.janecapstoneproject.MainActivity.STATION_INTERACTION_RADIUS_METERS;
import static com.example.janecapstoneproject.StationController.DEFAULT_ZOOM;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

public class MapController {
    public static final String TAG = "MapController";
    public static final int PUBLIC_CIRCLE_RGB = Color.rgb(0, 233, 255);
    public static final int PRIVATE_CIRCLE_RGB = Color.rgb(255, 0, 233);
    public static final int PUBLIC_MARKER_COLOR = 0;
    public static final int PRIVATE_MARKER_COLOR = 1;
    public static final int CURRENT_MARKER_COLOR = 2;
    private GoogleMap map;
    private Context context;
    private boolean firstInstance,bypassChecks;

    public MapController(Context context, GoogleMap map) {
        firstInstance=true;
        this.context = context;
        this.map = map;
    }

    //GETTERS, SETTERS
    public GoogleMap getMap() {
        return map;
    }
    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public void setBypassChecks(boolean bypassChecks) {
        this.bypassChecks = bypassChecks;
    }

    //MAP RENDERING
    @SuppressLint("MissingPermission")
    public void updateMapPositioning(Location location) throws IOException {
        if (bypassChecks) {
            float zoom;
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            if (firstInstance) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(),
                                location.getLongitude()), DEFAULT_ZOOM));
                firstInstance = false;
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(location.getLatitude(),
                                location.getLongitude())));
            }
            bypassChecks = false;
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
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    //STATION RENDERING ON MAP
    public void renderStation(Station station) {
        Log.d(TAG, station.getName());
        if (station != null && station.getCoords() != null) {
            if (station.isPublic()) {
                station.setMarker(map.addMarker(new MarkerOptions()
                        .position(station.getCoords())
                        .title(station.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastcyan))));
            } else {
                station.setMarker(map.addMarker(new MarkerOptions()
                        .position(station.getCoords())
                        .title(station.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastpurple))));
            }
            addCircleConventional(station, station.getCoords());
        }
    }
    public Marker addMarker(MarkerOptions markerOptions){
        return map.addMarker(markerOptions);
    }

    //CIRCLE RENDERING ON MAP
    public Circle addCircleConventional(Station station, LatLng coords) {
        if (station.isPublic()) {
            return addCircle(station, coords, PUBLIC_CIRCLE_RGB, STATION_INTERACTION_RADIUS_METERS);
        } else {
            return addCircle(station, coords, PRIVATE_CIRCLE_RGB, STATION_INTERACTION_RADIUS_METERS);
        }
    }
    public Circle addCircle(Station station, LatLng coords, int strokeColor, double radiusMeters) {
        return station.setCircleAndRetrieve(map.addCircle(new CircleOptions()
                .center(coords)
                .radius(radiusMeters)
                .strokeColor(strokeColor)
                .fillColor(Color.TRANSPARENT).strokeWidth(5.0F)));
    }
}