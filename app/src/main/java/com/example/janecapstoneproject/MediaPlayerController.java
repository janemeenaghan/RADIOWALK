package com.example.janecapstoneproject;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;
public class MediaPlayerController {
    private MediaPlayer mediaPlayer;
    private Context context;
    String currentURI;
    private static final String TAG = "MediaPlayerController";
    private ArrayList<MediaPlayerController.MediaPlayerCallback> callbacks = new ArrayList<>();
    public MediaPlayerController(Context context) {
        this.context = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                    mediaPlayer.release();
                }
                else {
                    mediaPlayer.reset();
                }
                return false;
            }
        });
    }
    public void terminatePlayer(){
        if (currentURI != null){
            currentURI = null;
        }
        if(mediaPlayer != null){
            reset();
            mediaPlayer.release();
        }
    }
    public void resume() {
        if(mediaPlayer == null){
            return;
        }
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }
    public void reset(){
        if (currentURI != null){
            currentURI = null;
        }
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }
        updateStateToMain();
    }
    public void setURLAndPrepare(String uriString) {
        if (uriString == null){
            return;
        }
        if (currentURI == null || !uriString.equals(currentURI)){
            try {
                reset();
                initNewMediaPlayer(uriString);
                currentURI = uriString;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void startPlaying(){
        if (mediaPlayer == null || currentURI == null){
            return;
        }
        if (!mediaPlayer.isPlaying()){
            if (currentURI != null) {
                mediaPlayer.start();
                updateStateToMain();
            }
        }
    }
    public void pause(){
        if (mediaPlayer == null){
            return;
        }
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        updateStateToMain();
    }
    public boolean isPlaying(){
        if (mediaPlayer== null){
            return false;
        }
        return mediaPlayer.isPlaying();
    }
    private void initNewMediaPlayer(String uriString) throws IOException {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(mp -> startPlaying());
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                for (MediaPlayerController.MediaPlayerCallback callback : callbacks) {
                    callback.onMediaPlayerError();
                }
                return false;
            }
        });
        if (uriString != null && !uriString.isEmpty()){
            mediaPlayer.setDataSource(context, Uri.parse(uriString));
            mediaPlayer.prepareAsync();
        }
        else{
        }
    }
    private void updateStateToMain(){
        for (MediaPlayerController.MediaPlayerCallback callback : callbacks) {
            if (currentURI == null){
                callback.onPlayingChanged(2);
            }
            else if (!mediaPlayer.isPlaying()){
                callback.onPlayingChanged(1);
            }
            else{
                callback.onPlayingChanged(0);
            }
        }
    }
    public void registerCallback(MediaPlayerController.MediaPlayerCallback mediaPlayerCallback){
        if (!callbacks.contains(mediaPlayerCallback)){
            callbacks.add(mediaPlayerCallback);
        }
    }
    public void unRegisterCallback(MediaPlayerController.MediaPlayerCallback mediaPlayerCallback){
        if (callbacks.contains(mediaPlayerCallback)){
            callbacks.remove(mediaPlayerCallback);
        }
    }
    public interface MediaPlayerCallback{
        void onPlayingChanged(int playingState);
        void onMediaPlayerError();
    }
}
