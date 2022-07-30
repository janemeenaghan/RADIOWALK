package com.example.janecapstoneproject;

import static com.example.janecapstoneproject.MainActivity.KEY_GEOPOINT;
import static com.example.janecapstoneproject.MainActivity.PRIVATE_TYPE;
import static com.example.janecapstoneproject.MainActivity.PUBLIC_TYPE;
import static com.example.janecapstoneproject.MainActivity.STATION_DETECTION_RADIUS_KILOMETERS;
import static com.example.janecapstoneproject.MainActivity.STATION_DETECTION_RADIUS_METERS;
import static com.example.janecapstoneproject.MapController.CURRENT_CIRCLE_RGB;
import static com.example.janecapstoneproject.MapController.CURRENT_MARKER_COLOR;
import static com.example.janecapstoneproject.MapController.PRIVATE_CIRCLE_RGB;
import static com.example.janecapstoneproject.MapController.PUBLIC_CIRCLE_RGB;
import static com.example.janecapstoneproject.Station.KEY_TYPE;
import static com.example.janecapstoneproject.Station.KEY_USERSSHAREDSTATIONS;

import android.content.Context;
import android.location.Location;

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
    public StationController(){
        publicPrivateSelection = -1;
    }
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
    public void renderClosestStation(Station closestStation) {
        if (!closestStation.hasMarker()){
            for (StationController.StationControllerCallback callback : callbacks) {
                closestStation.setMarker(callback.onRequestMarkerAddedToStation(closestStation,2));
            }
        }
        else {
            closestStation.setMarkerColor(CURRENT_MARKER_COLOR);
        }
        if(!closestStation.hasCircle()){
            for (StationController.StationControllerCallback callback : callbacks) {
                closestStation.setCircle(callback.onRequestCircleAddedToStation(closestStation,2));
            }
        }
        else{
            closestStation.setCircleColor(CURRENT_CIRCLE_RGB);
        }
        for (StationController.StationControllerCallback callback : callbacks) {
            callback.updateUIToRenderClosestStationStream(closestStation);
        }
    }
    public void renderStation(Station station) {
        if (!station.hasMarker()){
            for (StationController.StationControllerCallback callback : callbacks) {
                if (station.isPublic()) {
                    station.setMarker(callback.onRequestMarkerAddedToStation(station,0));
                }
                else{
                    station.setMarker(callback.onRequestMarkerAddedToStation(station,1));
                }
            }
        }
        else {
            if (station.isPublic()) {
                station.setMarkerColor(0);
            }
            else{
                station.setMarkerColor(1);
            }
        }
        if(!station.hasCircle()){
            for (StationController.StationControllerCallback callback : callbacks) {
                if (station.isPublic()) {
                    station.setCircle(callback.onRequestCircleAddedToStation(station,0));
                }
                else{
                    station.setCircle(callback.onRequestCircleAddedToStation(station,1));
                }
            }
        }
        else{
            if (station.isPublic()) {
                station.setCircleColor(PUBLIC_CIRCLE_RGB);
            }
            else{
                station.setCircleColor(PRIVATE_CIRCLE_RGB);
            }
        }
    }
    public void renderStationFromScratchIfRightType(Station station) {
        if ( (station.isPublic() && publicPrivateSelection == 1) || (station.isPrivate() && publicPrivateSelection == 0) ){
            return;
        }
        for (StationController.StationControllerCallback callback : callbacks) {
            if (station.isPublic()) {
                station.setMarker(callback.onRequestMarkerAddedToStation(station,0));
                station.setCircle(callback.onRequestCircleAddedToStation(station,0));
            }
            else{
                station.setMarker(callback.onRequestMarkerAddedToStation(station,1));
                station.setCircle(callback.onRequestCircleAddedToStation(station,1));
            }
        }
    }
    //Planned Problem aka Complex Feature #1
    public void queryAndRenderNearbyAndClosestStations(ParseUser user, Location location, double kiloRadius, double chaosFactor, String tag) throws IOException {
        ParseQuery<Station> query = ParseQuery.getQuery(Station.class);
        query.whereWithinKilometers(KEY_GEOPOINT, new ParseGeoPoint(location.getLatitude(), location.getLongitude()), kiloRadius);
        if (publicPrivateSelection == -1){
            return;
        }
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
                    return;
                }
                JSONArray usersStationsList = user.getJSONArray(KEY_USERSSHAREDSTATIONS);
                for (Station station : stations) {
                    if (tag.isEmpty()) {
                        renderFilteredNearbyStationsNoTag(station,location,usersStationsList,chaosFactor);
                    }
                    else{
                        renderFilteredNearbyStationsMatchingTag(tag,station,location,usersStationsList,chaosFactor);
                    }
                }
                handleBestStation(bestStation,location);
            }
        });
    }
    private void renderFilteredNearbyStationsNoTag(Station station, Location location, JSONArray usersStationsList, double chaosFactor){
        if (shouldIncludeStation(station, usersStationsList)) {
            if ((globalCurrentStation == null || !station.getObjectId().equals(globalCurrentStation.getObjectId()))) {
                renderStation(station);
            }
            computeScoreAndHandle (station, location, chaosFactor);
        }
        else{
            if (station.hasCircle()){
                station.removeCircle();

            }
            if (station.hasMarker()){
                station.removeMarker();
            }
        }
    }
    private void renderFilteredNearbyStationsMatchingTag(String tag, Station station, Location location, JSONArray usersStationsList, double chaosFactor){
        if (shouldIncludeStationWithTag(tag,station, usersStationsList)){
            if ((globalCurrentStation == null || !station.getObjectId().equals(globalCurrentStation.getObjectId()))) {
                renderStation(station);
            }
            computeScoreAndHandle (station, location, chaosFactor);
        }
        else{
            if (station.hasCircle()){
                station.removeCircle();

            }
            if (station.hasMarker()){
                station.removeMarker();
            }
        }
    }
    private void computeScoreAndHandle(Station station, Location location, double chaosFactor){
        double score = computeScore(station, location, globalCurrentStation, chaosFactor);
        if (score > highestScore) {
            highestScore = score;
            bestStation = station;
        } else {
        }
    }
    private void handleBestStation(Station bestStation, Location location){
        if (bestStation != null) {
            if (distance(bestStation, location) <= STATION_DETECTION_RADIUS_METERS) {
                for (StationController.StationControllerCallback callback : callbacks) {
                    callback.onCaseValidNearestStation(location, bestStation, (globalCurrentStationExists() && !bestStation.getObjectId().equals(globalCurrentStation.getObjectId())));
                }
                return;
            } else {
                for (StationController.StationControllerCallback callback : callbacks) {
                    callback.onCaseNoNearbyStation(location);
                }
            }
        } else {
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
            return 3;
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
        });
    }
    private void addStationToAUsersSharedList (Station station, ParseUser user){
        try {
            station.addThisToUsersSharedList(user);
            user.saveInBackground();

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    private void addUserToAStationsSharedList (Station station, ParseUser user){
        try {
            station.addUserToSharedList(user);
            station.saveInBackground();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    public void shareStationWithUser (Station station, ParseUser user){
        addStationToAUsersSharedList(station, user);
        addUserToAStationsSharedList(station, user);
    }
    public void unShareStationWithUser (Station station, ParseUser user){
        removeStationFromAUsersSharedList(station, user);
        removeUserFromAStationsSharedList(station, user);
    }
    private void removeStationFromAUsersSharedList (Station station, ParseUser user){
        try {
            station.removeThisFromUsersSharedList(user);
            station.saveInBackground();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    private void removeUserFromAStationsSharedList (Station station, ParseUser user){
        try {
            station.removeUserFromSharedList(user);
            user.saveInBackground();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
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
        Marker onRequestMarkerAddedToStation(Station station,int which);
        Circle onRequestCircleAddedToStation(Station station,int which);
        void updateUIToRenderClosestStationStream(Station station);
        void tellMapToClear();
    }
}
