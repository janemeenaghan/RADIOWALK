package com.example.janecapstoneproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakeStationsActivity extends AppCompatActivity implements PlaceForStationAdapter.OnStationListener {
    private GetDataService service;

    private List<PlaceForStation> placeForStationInfoListStorage;
    public static final String TAG = "MakeStationsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Create handle for the RetrofitInstance interface*/
        service = RetrofitClientInstanceForPlace.getRetrofitInstanceForPlace().create(GetDataService.class);
        ree();
    }

    public void ree() {
        Call<List<PlaceForStation>> call = service.getAllPlacesInArea();
        call.enqueue(new Callback<List<PlaceForStation>>() {
            @Override
            public void onResponse(Call<List<PlaceForStation>> call, Response<List<PlaceForStation>> response) {
                List<PlaceForStation> list = response.body();
                for (PlaceForStation placeForStation : list) {
                    try {
                        MainActivity.CHEESYCREATESTATIONSFROMPLACES(placeForStation.getName(),(new LatLng((double)placeForStation.getLatitude(),(double)placeForStation.getLongitude())));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
               // generateDataList(response.body());
            }

            @Override
            public void onFailure(Call<List<PlaceForStation>> call, Throwable t) {
                Toast.makeText(MakeStationsActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStationClick(int position) {

    }

}