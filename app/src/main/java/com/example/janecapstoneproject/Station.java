package com.example.janecapstoneproject;
import static com.example.janecapstoneproject.LoginActivity.TAG;

import android.util.Log;
import android.widget.RatingBar;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseClassName;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcel;

import java.lang.reflect.Array;
import java.util.Date;
@Parcel(analyze= Station.class)
@ParseClassName("Station")
public class Station extends ParseObject {
    public static final String KEY_GEOPOINT = "geopoint";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NAME = "name";
    public static final String KEY_STREAMLINK = "streamLink";
    public static final String KEY_USER = "user";
    public static final int PUBLIC_TYPE = 0;
    public static final int PRIVATE_TYPE = 1;

    public ParseGeoPoint getGeoPoint(){ return getParseGeoPoint(KEY_GEOPOINT);}
    public double getLatitude(){ return getParseGeoPoint(KEY_GEOPOINT).getLatitude();}
    public double getLongitude(){ return getParseGeoPoint(KEY_GEOPOINT).getLongitude();}
    public LatLng getCoords(){ return new LatLng(getLatitude(),getLongitude());}
    public int getType(){ return (int)getNumber(KEY_TYPE); }
    public boolean isPublic(){ return getType() == 0; }
    public boolean isPrivate(){ return getType() == 1; }
    public String getName(){ return getString(KEY_NAME); }
    public String getStreamLink(){ return getString(KEY_STREAMLINK); }
    public ParseUser getUser(){ return getParseUser(KEY_USER);}
    public void setGeoPoint(double latitude, double longitude){ put (KEY_GEOPOINT, new ParseGeoPoint(latitude,longitude)); }
    public void setGeoPoint(LatLng coords){ put (KEY_GEOPOINT, new ParseGeoPoint(coords.latitude,coords.longitude)); }
    public void setLatitude(double latitude){ put(KEY_GEOPOINT,new ParseGeoPoint(latitude,(getParseGeoPoint(KEY_GEOPOINT).getLongitude())));}
    public void setLongitude(double longitude){ put(KEY_GEOPOINT,new ParseGeoPoint((getParseGeoPoint(KEY_GEOPOINT).getLatitude()), longitude));}
    public void setPublic(){ put(KEY_TYPE, PUBLIC_TYPE); }
    public void setPrivate(){ put(KEY_TYPE, PRIVATE_TYPE); }
    public void setType(int type){ put(KEY_TYPE, type); }
    public void setName(String name){ put(KEY_NAME, name); }
    public void setStreamLink(String streamLink){ put(KEY_STREAMLINK, streamLink); }
    public void setUser(ParseUser user){ put(KEY_USER, user); }
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
}
