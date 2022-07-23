package com.example.janecapstoneproject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GetDataService {
    @GET("/json/stations?limit=20&order=votes&reverse=true&hidebroken=true")
    Call<List<StationInfo>> getAllStations();

    @GET("/json/stations/bytag/{tag}?limit=20&hidebroken=true")
    Call<List<StationInfo>> getStationsByTag(@Path("tag") String tag);
}