package com.example.janecapstoneproject;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

public class PlaceForStation {
    @SerializedName("results")
    private List<Result> results;
    public PlaceForStation(List<Result> results) {
        this.results = results;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public Result getResult(int i){ return results.get(i); }

    public Result setResult(int i, Result input){ return results.set(i,input); }

    public class Result{
        @SerializedName("name")
        private String name;
        @SerializedName("geometry")
        private Geometry geometry;
        public Result(String name, Geometry geometry){
            this.name = name;
            this.geometry = geometry;
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }
    }

    public class Geometry{
        @SerializedName("location")
        private Location location;

        public Geometry(Location location){
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    public class Location {
        @SerializedName("lat")
        private double latitude;
        @SerializedName("lng")
        private double longitude;

        public Location(int latitude, int longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
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
    }
}