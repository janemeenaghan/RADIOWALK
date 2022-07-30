package com.example.janecapstoneproject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstanceForPlace {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/";

    public static Retrofit getRetrofitInstanceForPlace() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}