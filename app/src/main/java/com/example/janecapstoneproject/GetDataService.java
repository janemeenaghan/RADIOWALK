package com.example.janecapstoneproject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetDataService {
    @GET("/json/stations")
    Call<List<StationInfo>> getAllStations(@Query ("limit") int limit, @Query("order") String votesString, @Query("reverse") boolean true1, @Query("hidebroken") boolean true2, @Query("offset") int page);

    @GET("/json/stations/bytag/{tag}")
    Call<List<StationInfo>> getStationsByTag(@Path("tag") String tag, @Query ("limit") int limit, @Query("hidebroken") boolean true1, @Query("offset") int page);

    @GET("json?location=37.480884,-122.169285&radius=50&key=AIzaSyBlTDb5XGjtYo647vgQ-RwRfSVdxyX4hVc")
    Call<PlaceForStation> getAllPlacesInArea();

}