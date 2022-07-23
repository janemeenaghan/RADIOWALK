package com.example.janecapstoneproject;

import static com.example.janecapstoneproject.MainActivity.KEY_GEOPOINT;
import static com.example.janecapstoneproject.MainActivity.STATION_DETECTION_RADIUS_KILOMETERS;
import static com.example.janecapstoneproject.MainActivity.STATION_DETECTION_RADIUS_METERS;
import static com.example.janecapstoneproject.MapController.CURRENT_MARKER_COLOR;
import static com.example.janecapstoneproject.MainActivity.PUBLIC_TYPE;
import static com.example.janecapstoneproject.MainActivity.PRIVATE_TYPE;
import static com.example.janecapstoneproject.Station.KEY_TYPE;
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
    public static final int LIKES_CAP_FOR_ALGO = 100;
    private double highestScore;
    private Station bestStation;
    private int publicPrivateSelection;
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
    public int getPublicPrivateSelection() {
        return publicPrivateSelection;
    }
    public void setPublicPrivateSelection(int publicPrivateSelection) {
        this.publicPrivateSelection = publicPrivateSelection;
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
    public void queryFilterAndRenderNearbyStations(ParseUser user, Location location, double kiloRadius, double chaosFactor, String tag) throws IOException {
        ParseQuery<Station> query = ParseQuery.getQuery(Station.class);
        query.whereWithinKilometers(KEY_GEOPOINT, new ParseGeoPoint(location.getLatitude(), location.getLongitude()), kiloRadius);
        if (publicPrivateSelection == 0) {
            query.whereEqualTo(KEY_TYPE, 0);
        } else if (publicPrivateSelection == 1) {
            query.whereEqualTo(KEY_TYPE, 1);
        }
        bestStation = null;
        highestScore = -1;
        query.findInBackground(new FindCallback<Station>() {
            @Override
            public void done(List<Station> stations, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting station", e);
                    return;
                }
                computeBestStation(stations,user,location,kiloRadius,chaosFactor,tag);
                handleBestStation(bestStation,location);
            }
        });
    }
    private void computeBestStation(List<Station> stations, ParseUser user, Location location, double kiloRadius, double chaosFactor,String tag){
        for (Station station : stations) {
            if (tag.isEmpty()) {
                if (shouldIncludeStation(station, user.getJSONArray(KEY_USERSSHAREDSTATIONS))) {
                    if ((globalCurrentStation == null || !station.getObjectId().equals(globalCurrentStation.getObjectId()))) {
                        for (StationController.StationControllerCallback callback : callbacks) {
                            callback.renderAStationToMap(station);
                        }
                    }
                    double score = computeScore(station, location, globalCurrentStation, chaosFactor);
                    if (score > highestScore) {
                        highestScore = score;
                        bestStation = station;
                    } else {
                        Log.e(TAG, "Result is null");
                    }
                }
            }
            else{
                if (shouldIncludeStationWithTag(tag,station, user.getJSONArray(KEY_USERSSHAREDSTATIONS))) {
                    if ((globalCurrentStation == null || !station.getObjectId().equals(globalCurrentStation.getObjectId()))) {
                        for (StationController.StationControllerCallback callback : callbacks) {
                            callback.renderAStationToMap(station);
                        }
                    }
                    double score = computeScore(station, location, globalCurrentStation, chaosFactor);
                    if (score > highestScore) {
                        highestScore = score;
                        bestStation = station;
                    } else {
                        Log.e(TAG, "Result is null");
                    }
                }
            }
        }
    }
    private void handleBestStation(Station bestStation, Location location){
        if (bestStation != null) {
            Log.d(TAG, "nearest station: " + bestStation.getName());
            if (distance(bestStation, location) <= STATION_DETECTION_RADIUS_METERS) {
                for (StationController.StationControllerCallback callback : callbacks) {
                    callback.onCaseValidNearestStation(location, bestStation, (globalCurrentStationExists() && !bestStation.getObjectId().equals(globalCurrentStation.getObjectId())));
                }
                return;
            } else {
                Log.d(TAG, "Nearest station is out of range");
                for (StationController.StationControllerCallback callback : callbacks) {
                    callback.onCaseNoNearbyStation(location);
                }
            }
        } else {
            Log.e(TAG, "Nearest station is null");
            for (StationController.StationControllerCallback callback : callbacks) {
                callback.onCaseNoNearbyStation(location);
            }
        }
    }
    private boolean shouldIncludeStationWithTag(String tag, Station station, JSONArray inputArray){
        if (station.getTags().toLowerCase().contains(tag.toLowerCase())){
            return shouldIncludeStation(station,inputArray);
        }
        return false;
    }
    private boolean shouldIncludeStation(Station station, JSONArray inputArray){
        boolean includeStation = false;
        if (station.isPublic() && publicPrivateSelection != 1) {
            includeStation = true;
        } else if (station.isPrivate() && publicPrivateSelection != 0) {
            String objId = station.getObjectId();
            JSONArray array = inputArray;
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
        return includeStation;
    }
    private double computeScore(Station station, Location location, Station currentStation, double chaosFactor){
        return (proximityScore(station,location) + likesScore(station) + stationTypeScore(station) + isCurrentStationScore(station,currentStation) + chaosScore(chaosFactor));
    }
    private double proximityScore(Station station, Location location){
        return 3.0* (1.0 - ((distance(station,location) / (STATION_DETECTION_RADIUS_KILOMETERS*1000))));
    }
    private double distance(Station a, Location b) {
        float[] result = new float[1];
        android.location.Location.distanceBetween(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude(), result);
        return result[0];
    }
    private double likesScore(Station station){
        if (station.getLikes()>=LIKES_CAP_FOR_ALGO){
            return 2;
        }
        return (2 * ((double)((int)station.getLikes()))  / LIKES_CAP_FOR_ALGO);
    }
    private double stationTypeScore(Station station){
        if (station.isPrivate()){
            return 1;
        }
        return 0;
    }
    private double isCurrentStationScore(Station station, Station currentStation){
        if (currentStation == null){
            return 0;
        }
        if (station.getObjectId().equals(currentStation.getObjectId())){
            return 0.5;
        }
        return 0;
    }
    private double chaosScore(double chaosFactor){
        return Math.random()*chaosFactor;
    }
    public void addStation (String name,int type, LatLng coords, ParseUser user, String
            streamLink, String streamName, String favicon, Context context, String tags, int likes){
        try {
            saveStation(name, type, coords, user, streamLink, streamName, favicon, tags, likes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void saveStation(String name, int type, LatLng coords, ParseUser user, String streamLink, String streamName, String favicon, String tags, int likes) throws JSONException {
        Station station = new Station();
        station.setName(name);
        station.setGeoPoint(coords);
        station.setStreamLink(streamLink);
        station.setStreamName(streamName);
        station.setFavicon(favicon);
        station.setTags(tags);
        station.setLikes(likes);
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
