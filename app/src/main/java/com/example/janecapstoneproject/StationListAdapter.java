package com.example.janecapstoneproject;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import org.parceler.Parcels;
import java.util.List;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.CustomViewHolder> {

    private List<StationInfo> dataList;
    private Context context;
    private OnStationListener mOnStationListener;

    public StationListAdapter(Context context,List<StationInfo> dataList, OnStationListener onStationListener){
        this.context = context;
        this.dataList = dataList;
        this.mOnStationListener = onStationListener;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        ImageView faviconImageView;
        android.widget.TextView nameTextView;
        TextView tagsTextView;
        ImageView likeCountImageView;
        TextView likeCountTextView;
        OnStationListener onStationListener;

        CustomViewHolder(View itemView, OnStationListener onStationListener) {
            super(itemView);
            mView = itemView;
            faviconImageView = mView.findViewById(R.id.faviconImageView);
            nameTextView = mView.findViewById(R.id.nameTextView);
            tagsTextView = mView.findViewById(R.id.tagsTextView);
            likeCountImageView = mView.findViewById(R.id.likeCountImageView);
            likeCountTextView = mView.findViewById(R.id.likeCountTextView);
            this.onStationListener = onStationListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onStationListener.onStationClick(getAdapterPosition());
        }
    }

    public interface OnStationListener{
        void onStationClick(int position);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.station_option_item, parent, false);
        return new CustomViewHolder(view,mOnStationListener);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        holder.nameTextView.setText(dataList.get(position).getName());
        holder.tagsTextView.setText(dataList.get(position).getTags());

        StationInfo stationInfo = dataList.get(position);
        int i = stationInfo.getVotes();
        String string = ""+i;
        holder.likeCountTextView.setText(string);
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(new OkHttp3Downloader(context));
        String favicon = dataList.get(position).getFavicon();
        if (favicon != null && !favicon.trim().isEmpty()){
            builder.build().load(favicon)
                    .placeholder((R.drawable.ic_launcher_background))
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.faviconImageView);
        }
        else {
            builder.build().load(R.drawable.ic_launcher_background)
                    .placeholder((R.drawable.ic_launcher_background))
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.faviconImageView);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}