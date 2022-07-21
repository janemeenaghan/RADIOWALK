package com.example.janecapstoneproject;

import static com.example.janecapstoneproject.MainActivity.KEY_GEOPOINT;
import static com.example.janecapstoneproject.MainActivity.STATION_DETECTION_RADIUS_KILOMETERS;
import static com.example.janecapstoneproject.MainActivity.STATION_INTERACTION_RADIUS_METERS;
import static com.example.janecapstoneproject.Station.KEY_USERSSHAREDSTATIONS;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StationController {
    public static final String TAG = "StationController";
    Station nearestStation;
    float shortestDistance;
    //private ArrayList<StationController.StationControllerCallback> callbacks = new ArrayList<>();
    StationController.StationControllerCallback myCallback;

    public StationController(){

    }


    public Circle addCircleAndReturnIt(Station station, LatLng coords) {

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

    private void renderNearbyStations(Context context, Location location, ) throws IOException {
        ParseUser user = ParseUser.getCurrentUser();
        // specify what type of data we want to query - Station.class
        ParseQuery<Station> query = ParseQuery.getQuery(Station.class);
        //query.setLimit(20);
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
                        myCallback.onCaseValidNearestStation(location,nearestStation);
                        return;
                    } else {
                        Log.d(TAG, "Nearest station is too far!");
                        myCallback.onCaseNoNearbyStation(location);
                    }
                } else {
                    Log.e(TAG, "Nearest station is null", e);
                    myCallback.onCaseNoNearbyStation(location);
                }
            }
        });
    }

    public interface StationControllerCallback{
        void onCaseNoNearbyStation(Location location);
        void onCaseValidNearestStation(Location location, Station nearestStation);
    }
}
