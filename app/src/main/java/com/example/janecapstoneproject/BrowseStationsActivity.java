package com.example.janecapstoneproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseStationsActivity extends AppCompatActivity implements StationListAdapter.OnStationListener {
    public static final String TAG = "BrowseStationsActivity";
    public static final int RESULTS_PER_PAGE = 20;
    public static final int FETCH_ALL_MODE = 0;
    public static final int SEARCH_MODE = 1;
    private RecyclerView stationRecycler;
    private StationListAdapter adapter;
    private SearchView searchView;
    private RecyclerView.LayoutManager layoutManager;
    private GetDataService service;
    private List<StationInfo> stationInfoListStorage;
    private int mode;
    private int isNew;
    private LatLng latLng;
    private String globalTag;
    private String stationName;
    private ProgressDialog progressDialog, referenceToProgressDialogShow;
    private Button nextButton,prevButton;
    private int globalPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_stations);
        isNew = getIntent().getIntExtra("new",2);
        latLng = Parcels.unwrap(getIntent().getParcelableExtra("latLng"));
        stationName = getIntent().getStringExtra("stationName");
        globalPage = 0;
        service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        mode = FETCH_ALL_MODE;
        fetchAllStations(globalPage);
        initSearchView();
        initPaging();
    }
    //Planned Problem aka Complex Feature #2
    public void fetchAllStations(int page) {
        progressDialog = new ProgressDialog(BrowseStationsActivity.this);
        progressDialog.setMessage("Loading....");
        referenceToProgressDialogShow =progressDialog.show(this, "Loading","Please wait a few seconds....");
        Call<List<StationInfo>> call = service.getAllStations(RESULTS_PER_PAGE,"votes",true,true,RESULTS_PER_PAGE*page);
        call.enqueue(new Callback<List<StationInfo>>() {
            @Override
            public void onResponse(Call<List<StationInfo>> call, Response<List<StationInfo>> response) {
                progressDialog.dismiss();
                referenceToProgressDialogShow.dismiss();
                List<StationInfo> list = response.body();
                generateDataList(response.body());
            }

            @Override
            public void onFailure(Call<List<StationInfo>> call, Throwable t) {
                progressDialog.dismiss();
                referenceToProgressDialogShow.dismiss();
                Toast.makeText(BrowseStationsActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void fetchStationsByTag(String tag, int page){
        globalTag = tag;
        Call<List<StationInfo>> call = service.getStationsByTag(tag,RESULTS_PER_PAGE,true,RESULTS_PER_PAGE*page);
        call.enqueue(new Callback<List<StationInfo>>() {
            @Override
            public void onResponse(Call<List<StationInfo>> call, Response<List<StationInfo>> response) {
                progressDialog.dismiss();
                referenceToProgressDialogShow.dismiss();
                generateDataList(response.body());
            }
            @Override
            public void onFailure(Call<List<StationInfo>> call, Throwable t) {
                progressDialog.dismiss();
                referenceToProgressDialogShow.dismiss();
                Toast.makeText(BrowseStationsActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void initSearchView(){
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                globalPage = 0;
                fetchStationsByTag(query,globalPage);
                return false;
            }
        });
    }
    private void initPaging(){
        nextButton.findViewById(R.id.nextButtonBrowse);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == FETCH_ALL_MODE){
                    fetchAllStations(globalPage +1);
                }
                else if (mode == SEARCH_MODE){
                    if (globalTag != null) {
                        fetchStationsByTag(globalTag, globalPage + 1);
                    }
                }
            }});
        prevButton.findViewById(R.id.prevButtonBrowse);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == FETCH_ALL_MODE && globalPage > 0){
                    fetchAllStations(globalPage -1);
                }
                else if (mode == SEARCH_MODE) {
                    if (globalTag != null && globalPage > 0) {
                        fetchStationsByTag(globalTag, globalPage - 1);
                    }
                }
            }});
    }
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
        StationInfo selectedStationInfo = stationInfoListStorage.get(position);
        Intent intent = new Intent();
        intent.putExtra("new",isNew);
        intent.putExtra("stationName",stationName);
        intent.putExtra("name", selectedStationInfo.getName());
        intent.putExtra("url",selectedStationInfo.getUrl());
        intent.putExtra("favicon",selectedStationInfo.getFavicon());
        intent.putExtra("tags",selectedStationInfo.getTags());
        intent.putExtra("likes",selectedStationInfo.getVotes());
        intent.putExtra("latLng",Parcels.wrap(latLng));
        setResult(RESULT_OK, intent);
        finish();
    }
}