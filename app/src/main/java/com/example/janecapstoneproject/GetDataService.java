package com.example.janecapstoneproject;
import java.util.List;

import retrofit2.*;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetDataService {
    @GET("/json/stations")
    Call<List<StationInfo>> getAllStations();

    @GET("/json/stations/bytag/{tag}")
    Call<List<StationInfo>> getStationsByTag(@Path("tag") String tag);

    @GET("json?location=37.4816121,-122.1687706&radius=5&key=AIzaSyBlTDb5XGjtYo647vgQ-RwRfSVdxyX4hVc")
    Call<List<PlaceForStation>> getAllPlacesInArea();

}