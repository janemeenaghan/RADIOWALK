package com.example.janecapstoneproject;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;
public class StationInfo {
    @SerializedName("stationuuid")
    private UUID stationuuid;
    @SerializedName("name")
    private String name;
    @SerializedName("tags")
    private String tags;
    @SerializedName("url_resolved")
    private String url;
    @SerializedName("favicon")
    private String favicon;
    @SerializedName("votes")
    private int votes;
    public StationInfo(UUID stationuuid, String name, String tags, String url, String favicon, int votes){
        this.stationuuid = stationuuid;
        this.name = name;
        this.tags = tags;
        this.url = url;
        this.favicon = favicon;
        this.votes = votes;
    }
    public UUID getStationuuid() {
        return stationuuid;
    }
    public String getName() {
        return name;
    }
    public String getTags() {
        return tags;
    }
    public String getUrl() {
        return url;
    }
    public String getFavicon() {
        return favicon;
    }
    public int getVotes() {
        return votes;
    }
    public void setStationuuid(UUID stationuuid) {
        this.stationuuid = stationuuid;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }
    public void setVotes(int votes) {
        this.votes = votes;
    }
}
