package com.example.janecapstoneproject;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import org.parceler.Parcels;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseStationsActivity extends AppCompatActivity implements StationListAdapter.OnStationListener {
    private RecyclerView stationRecycler/*, tagsRecycler*/;
    private StationListAdapter adapter;
    private SearchView searchView;
    private RecyclerView.LayoutManager layoutManager;
    private GetDataService service;
    private List<StationInfo> stationInfoListStorage;
    public static final String TAG = "BrowseStationsActivity";
    private int isNew;
    private LatLng latLng;
    private String stationName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isNew = getIntent().getIntExtra("new",2);
        if (isNew==1) {
            Log.e(TAG, "onCreate: New");
        }
        else if (isNew == 0){
            Log.e(TAG, "onCreate: Not new");
        }
        else {
            Log.e(TAG, "onCreate: new != 0 or 1, something is wrong (maybe add Parcels.unwrap?");
        }
        latLng = Parcels.unwrap(getIntent().getParcelableExtra("latLng"));
        stationName = getIntent().getStringExtra("stationName");
        setContentView(R.layout.activity_browse_stations);
        //generateGenreList();
        //Progress loader or some placeholder goes here

        /*Create handle for the RetrofitInstance interface*/
        service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        fetchAllStations();
        initSearchView();
    }

    public void fetchAllStations() {
        Call<List<StationInfo>> call = service.getAllStations();
        call.enqueue(new Callback<List<StationInfo>>() {
            @Override
            public void onResponse(Call<List<StationInfo>> call, Response<List<StationInfo>> response) {
                List<StationInfo> list = response.body();
                for (StationInfo stationInfo : list) {
                    Log.e("kys", stationInfo.getName());
                }
                //dismiss progress loader
                generateDataList(response.body());
            }

            @Override
            public void onFailure(Call<List<StationInfo>> call, Throwable t) {
                //dismiss progress loader
                Toast.makeText(BrowseStationsActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void fetchStationsByTag(String tag){
        Call<List<StationInfo>> call = service.getStationsByTag(tag);
        call.enqueue(new Callback<List<StationInfo>>() {
            @Override
            public void onResponse(Call<List<StationInfo>> call, Response<List<StationInfo>> response) {
                //dismiss progress loader
                generateDataList(response.body());
            }

            @Override
            public void onFailure(Call<List<StationInfo>> call, Throwable t) {
                //dismiss progress loader
                Toast.makeText(BrowseStationsActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void initSearchView(){
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.e("onQueryTextChange", "==called");
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Do something
                fetchStationsByTag(query);
                return false;
            }

        });
    }
    /*private void generateGenreList(){
        tagsRecycler = findViewById(R.id.tagsRecycler);
        //adapter setup
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        tagsRecycler.setLayoutManager(layoutManager);
        //tagsRecycler.setAdapter(someadapter);
    }*/
    /*Method to generate List of data using RecyclerView with custom adapter*/
    private void generateDataList(List<StationInfo> stationList) {
        stationInfoListStorage = stationList;
        stationRecycler = findViewById(R.id.stationRecycler);
        adapter = new StationListAdapter(this,stationList,this);
        layoutManager = new LinearLayoutManager(BrowseStationsActivity.this);
        stationRecycler.setLayoutManager(layoutManager);
        stationRecycler.setAdapter(adapter);
    }

    @Override
    public void onStationClick(int position) {
        //if bugs make sure this storage is the right way to find the position; https://www.youtube.com/watch?v=69C1ljfDvl0&t=211
        StationInfo selectedStationInfo = stationInfoListStorage.get(position);
        Intent intent = new Intent();
        intent.putExtra("new",isNew);
        intent.putExtra("stationName",stationName);
        intent.putExtra("name", selectedStationInfo.getName());
        intent.putExtra("url",selectedStationInfo.getUrl());
        intent.putExtra("favicon",selectedStationInfo.getFavicon());
        if (isNew != 0 && isNew != 1){
            Log.e(TAG, "Returning new value != 0 or 1, was already this value when passed to Browse");
        }
        intent.putExtra("latLng",Parcels.wrap(latLng));
        setResult(RESULT_OK, intent);
        finish();
    }
}