package com.example.janecapstoneproject;
import static com.example.janecapstoneproject.LoginActivity.TAG;

import android.graphics.Color;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RatingBar;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcel;

import java.lang.reflect.Array;
import java.util.Date;
//@Parcel(analyze= Station.class)
@ParseClassName("Station")
public class Station extends ParseObject {
//    Circle circle;
    public static final String KEY_GEOPOINT = "geopoint";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NAME = "name";
    public static final String KEY_STREAMLINK = "streamLink";
    public static final String KEY_STREAMNAME = "streamName";
    public static final String KEY_OBJECTID = "objectId";
    public static final String KEY_FAVICON = "favicon";
    public static final String KEY_USERSSHAREDWITH = "usersSharedWith";
    public static final String KEY_USERSSHAREDSTATIONS = "sharedStations";
    public static final String KEY_USER = "user";
    public static final int PUBLIC_TYPE = 0;
    public static final int PRIVATE_TYPE = 1;
    private Circle circle;
    private Marker marker;

    public Station(){

    }
    public Station(String objectId) throws ParseException {
        ParseQuery<Station> query = ParseQuery.getQuery(Station.class);
        query.whereEqualTo(KEY_OBJECTID,objectId);
        query.find();
    }
    public ParseGeoPoint getGeoPoint(){ return getParseGeoPoint(KEY_GEOPOINT);}
    public double getLatitude(){ return getParseGeoPoint(KEY_GEOPOINT).getLatitude();}
    public double getLongitude(){ return getParseGeoPoint(KEY_GEOPOINT).getLongitude();}
    public LatLng getCoords(){ return new LatLng(getLatitude(),getLongitude());}
    public int getType(){ return (int)getNumber(KEY_TYPE); }
    public boolean isPublic(){ return getType() == 0; }
    public boolean isPrivate(){ return getType() == 1; }
    public String getName(){ return getString(KEY_NAME); }
    public String getStreamLink(){ return getString(KEY_STREAMLINK); }
    public String getStreamName(){ return getString(KEY_STREAMNAME); }
    public String getFavicon(){
        if (getString(KEY_FAVICON) == null){
            return "";
        }
        else{
            return getString(KEY_FAVICON);
        }
    }
    public ParseUser getUser(){ return getParseUser(KEY_USER);}
    public JSONArray getUsersSharedWith(){
        return getJSONArray(KEY_USERSSHAREDWITH);
    }
    public Circle getCircle(){ return circle; }
    public Marker getMarker(){ return marker; }
    public void setGeoPoint(double latitude, double longitude){ put (KEY_GEOPOINT, new ParseGeoPoint(latitude,longitude));  }
    public void setGeoPoint(LatLng coords){ put (KEY_GEOPOINT, new ParseGeoPoint(coords.latitude,coords.longitude));  }
    public void setLatitude(double latitude){ put(KEY_GEOPOINT,new ParseGeoPoint(latitude,(getParseGeoPoint(KEY_GEOPOINT).getLongitude()))); }
    public void setLongitude(double longitude){ put(KEY_GEOPOINT,new ParseGeoPoint((getParseGeoPoint(KEY_GEOPOINT).getLatitude()), longitude));}
    public void setPublic(){ put(KEY_TYPE, PUBLIC_TYPE); }
    public void setPrivate(){ put(KEY_TYPE, PRIVATE_TYPE); }
    public void setType(int type){ put(KEY_TYPE, type);  }
    public void setName(String name){ put(KEY_NAME, name);  }
    public void setStreamLink(String streamLink){ put(KEY_STREAMLINK, streamLink); }
    public void setStreamName(String streamName){ put(KEY_STREAMNAME, streamName); }
    public void setFavicon(String favicon){ put(KEY_FAVICON,favicon); }
    public void setUser(ParseUser user){ put(KEY_USER, user);}
    public void setCircle(Circle circle) {
        this.circle = circle;
    }
    public Circle setCircleAndReturnIt(Circle circle) {
        this.circle = circle;
        return this.circle;
    }
    public void setCircleColor(int rgb){
        circle.setStrokeColor(rgb);
    }
    /*public void handleSetMarkerColor(int which){

    }*/
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    public Marker setMarkerAndReturnIt(Marker marker) {
        this.marker = marker;
        return this.marker;
    }

    public void setMarkerColor(int which){
        if (marker != null){
            if (which == 0){
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastcyan));
            }
            else if (which == 1){
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastpurple));
            }
            else{
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastgreen));
            }
        }
        else{
            if (which == 0){
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastcyan));
            }
            else if (which == 1){
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastpurple));
            }
            else{
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.broadcastgreen));
            }
        }
    }
    public void addUserToSharedList(ParseUser user) throws JSONException {
        if (getJSONArray(KEY_USERSSHAREDWITH)==null){
            JSONArray array = new JSONArray().put(user.getObjectId());
            put(KEY_USERSSHAREDWITH, array);
        }
        else{
            JSONArray array = getJSONArray(KEY_USERSSHAREDWITH).put(user.getObjectId());
            put(KEY_USERSSHAREDWITH, array);
        }

    }
    public void addThisToUsersSharedList(ParseUser user) throws JSONException {
        if (user.getJSONArray(KEY_USERSSHAREDSTATIONS)==null){
            JSONArray array = new JSONArray().put(this.getObjectId());
            user.put(KEY_USERSSHAREDSTATIONS, array);
        }
        else{
            JSONArray array = user.getJSONArray(KEY_USERSSHAREDSTATIONS).put(this.getObjectId());
            user.put(KEY_USERSSHAREDSTATIONS, array);
        }
    }

    public void updateStationWithNewRadioToParse(String streamLink, String streamName, String favicon){
        setStreamLink(streamLink);
        setStreamName(streamName);
        setFavicon(favicon);
        saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
    }
    /*public boolean thisIsIncludedInUsersSharedList(ParseUser user){
        JSONArray array = user.getJSONArray(KEY_USERSSHAREDSTATIONS);

    }*/


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Station) {
            return getObjectId().equals(((Station) obj).getObjectId());
        }
        return false;
        //return super.equals(obj);
    }

    public static String calculateTimeAgo(Date createdAt) {
        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;
        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();
            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }
        return "";
    }

    public interface StationCallback{
        void onSaveInBackground();
    }

}
