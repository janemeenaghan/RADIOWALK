package com.example.janecapstoneproject;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class PlaceForStation {
    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;
    //will need additional work to separate tagsRecycler by commas into a list. or see if retrofit can separate for you
    @SerializedName("name")
    private String name;

    public PlaceForStation(String name, int latitude, int longitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
