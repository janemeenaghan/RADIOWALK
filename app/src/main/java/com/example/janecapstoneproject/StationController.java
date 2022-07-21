package com.example.janecapstoneproject;

import static com.example.janecapstoneproject.MainActivity.KEY_GEOPOINT;
import static com.example.janecapstoneproject.MapController.CURRENT_MARKER_COLOR;
import static com.example.janecapstoneproject.MainActivity.PUBLIC_TYPE;
import static com.example.janecapstoneproject.MainActivity.PRIVATE_TYPE;
import static com.example.janecapstoneproject.MainActivity.STATION_INTERACTION_RADIUS_METERS;
import static com.example.janecapstoneproject.Station.KEY_USERSSHAREDSTATIONS;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
    private Station globalCurrentStation;
    public static final int DEFAULT_ZOOM = 15;
    private Station nearestStation;
    private float shortestDistance;
    private ArrayList<StationController.StationControllerCallback> callbacks = new ArrayList<>();

    public Station getGlobalCurrentStation() {
        return globalCurrentStation;
    }

    public void setGlobalCurrentStation(Station globalCurrentStation) {
        this.globalCurrentStation = globalCurrentStation;
    }

    public void setGlobalCurrentStationMarkerColor(int which) {
        globalCurrentStation.setMarkerColor(which);
    }

    public boolean globalCurrentStationExists() {
        return globalCurrentStation != null;
    }

    public void renderClosestStation(Context context, Station closestStation) {
        if (closestStation.getMarker() == null){
            for (StationController.StationControllerCallback callback : callbacks) {
                closestStation.setMarker(callback.onRequestMarkerAddedToClosestStation(closestStation));
            }
        }
        else {
            closestStation.setMarkerColor(CURRENT_MARKER_COLOR);
        }
        if(closestStation.getCircle() == null){
            for (StationController.StationControllerCallback callback : callbacks) {
                closestStation.setCircle(callback.onRequestCircleAddedToClosestStation(closestStation));
            }
        }
        for (StationController.StationControllerCallback callback : callbacks) {
            callback.updateUIToRenderClosestStation(closestStation);
        }
    }

    public void renderNearbyStations(ParseUser user, Context context, Location location, double kiloRadius) throws IOException {
        ParseQuery<Station> query = ParseQuery.getQuery(Station.class);
        //query.setLimit(20);
        query.whereWithinKilometers(KEY_GEOPOINT, new ParseGeoPoint(location.getLatitude(), location.getLongitude()), kiloRadius);
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
                            for (StationController.StationControllerCallback callback : callbacks) {
                                callback.renderAStationToMap(station);
                            }
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
                        for (StationController.StationControllerCallback callback : callbacks) {
                            callback.onCaseValidNearestStation(location,nearestStation,(globalCurrentStationExists() && !nearestStation.getObjectId().equals(globalCurrentStation.getObjectId())));
                        }
                        return;
                    } else {
                        Log.d(TAG, "Nearest station is too far!");
                        for (StationController.StationControllerCallback callback : callbacks) {
                            callback.onCaseNoNearbyStation(location);
                        }
                    }
                } else {
                    Log.e(TAG, "Nearest station is null", e);
                    for (StationController.StationControllerCallback callback : callbacks) {
                        callback.onCaseNoNearbyStation(location);
                    }
                }
            }
        });
    }

    public void addStation (String name,int type, LatLng coords, ParseUser user, String
            streamLink, String streamName, String favicon, Context context){
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
                    for (StationController.StationControllerCallback callback : callbacks) {
                        callback.onSaveStation(station,user);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            if (e != null) {
                Log.e(TAG, "Error while saving", e);
            }
            else{
                Log.d(TAG, "Station save was successful!");
            }
        });
    }

    private void addStationToAUsersSharedList (Station station, ParseUser user){
        try {
            station.addThisToUsersSharedList(user);
            station.saveInBackground();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void addUserToAStationsSharedList (Station station, ParseUser user){
        try {
            station.addUserToSharedList(user);
            user.saveInBackground();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void shareStationWithUser (Station station, ParseUser user){
        addStationToAUsersSharedList(station, user);
        addUserToAStationsSharedList(station, user);
    }

    public void registerCallback(StationController.StationControllerCallback stationControllerCallback){
        if (!callbacks.contains(stationControllerCallback)){
            callbacks.add(stationControllerCallback);
        }
    }

    public void unRegisterCallback(StationController.StationControllerCallback stationControllerCallback){
        if (callbacks.contains(stationControllerCallback)){
            callbacks.remove(stationControllerCallback);
        }
    }

    public interface StationControllerCallback{
        void onCaseNoNearbyStation(Location location);
        void onCaseValidNearestStation(Location location, Station nearestStation, boolean needToDeselectCurrentStation);
        void onSaveStation(Station station,ParseUser user) throws JSONException;
        Marker onRequestMarkerAddedToClosestStation(Station closestStation);
        Circle onRequestCircleAddedToClosestStation(Station closestStation);
        void updateUIToRenderClosestStation(Station station);
        void renderAStationToMap(Station station);

    }
}
