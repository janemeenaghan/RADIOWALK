package com.example.janecapstoneproject;
import java.util.List;

import retrofit2.*;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GetDataService {

    @GET("/json/stations")
    Call<List<StationInfo>> getAllStations();

    @GET("/json/stations/bytag/{tag}")
    Call<List<StationInfo>> getStationsByTag(@Path("tag") String tag);
}