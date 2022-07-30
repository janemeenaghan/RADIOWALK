package com.example.janecapstoneproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rey.material.widget.Button;
import com.rey.material.widget.ImageView;
import com.rey.material.widget.TextView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class StationUserListAdapter extends RecyclerView.Adapter<StationUserListAdapter.CustomViewHolder> {
    private Context context;
    private OnStationUserListener mOnStationUserListener;
    private JSONArray usersSharedWith;
    private List<ParseUser> usersSharedWithAsParseUsers;
    private Station station;
    private boolean isOwner;
    public StationUserListAdapter(Context context,Station station, OnStationUserListener onStationUserListener, boolean isOwner) throws JSONException, ParseException {
        this.station = station;
        this.context = context;
        this.isOwner = isOwner;
        this.mOnStationUserListener = onStationUserListener;
        if (station.isPrivate()) {
            usersSharedWithAsParseUsers = new ArrayList<ParseUser>();
            ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
            usersSharedWith = station.getUsersSharedWith();
            for (int i = 0; i < usersSharedWith.length(); i++) {
                query.whereEqualTo("objectId", usersSharedWith.get(i));
                ParseUser user = null;
                if ((user = query.getFirst()) != null) {
                    usersSharedWithAsParseUsers.add(user);
                }
            }
        }
    }
    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        TextView nameTextView;
        ImageView removeUserFromStationIcon;
        StationUserListAdapter.OnStationUserListener onStationUserListener;
        CustomViewHolder(View itemView, OnStationUserListener onStationUserListener) {
            super(itemView);
            mView = itemView;
            nameTextView = mView.findViewById(R.id.stationUserName);
            removeUserFromStationIcon = mView.findViewById(R.id.removeUserFromStationIcon);
            this.onStationUserListener = onStationUserListener;
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            onStationUserListener.onStationUserClick(getAdapterPosition());
        }
    }
    public interface OnStationUserListener{
        void onStationUserClick(int position);
        void onStationUserRemoveClick(ParseUser user,Station station);
    }
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.station_user_item, parent, false);
        return new CustomViewHolder(view,mOnStationUserListener);
    }
    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        if (station.isPrivate()) {
            int thisPosition = position;
            ParseUser user = usersSharedWithAsParseUsers.get(position);
            holder.removeUserFromStationIcon.setVisibility(View.INVISIBLE);
            holder.nameTextView.setText(user.getUsername());
            if (station.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                holder.removeUserFromStationIcon.setVisibility(View.VISIBLE);
                holder.removeUserFromStationIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnStationUserListener.onStationUserRemoveClick(usersSharedWithAsParseUsers.get(thisPosition), station);
                    }
                });
            }
        }
    }
    @Override
    public int getItemCount() {
        return usersSharedWithAsParseUsers.size();
    }
}