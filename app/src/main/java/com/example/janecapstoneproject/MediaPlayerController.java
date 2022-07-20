package com.example.janecapstoneproject;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerController {
    private MediaPlayer mediaPlayer;
    private Context context;
    String currentURI;
    private static final String TAG = "MediaPlayerController";
    private ArrayList<MediaPlayerCallback> callbacks = new ArrayList<>();

    public MediaPlayerController(Context context){
        this.context = context;
    }

    public void registerCallback(MediaPlayerCallback mediaPlayerCallback){
        if (!callbacks.contains(mediaPlayerCallback)){
            callbacks.add(mediaPlayerCallback);
        }
    }

    public void unRegisterCallback(MediaPlayerCallback mediaPlayerCallback){
        if (callbacks.contains(mediaPlayerCallback)){
            callbacks.remove(mediaPlayerCallback);
        }
    }

    public void setURLAndPrepare(String uriString,boolean bypass) {
        boolean proceed = false;
        if (currentURI == null){
            proceed = true;
        }
        else if (currentURI.trim().isEmpty()) {
            proceed = true;
        }
        else if (bypass){
            proceed = true;
        }
        else if (uriString == null) {

        }
        else if (!uriString.equals(currentURI)) {
            proceed = true;
        }
        else{

        }
        if (proceed){
            try {
                currentURI = uriString;
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                initNewMediaPlayer(context, uriString);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setURLAndPrepare(Uri uri) {
        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPlaying(){
        mediaPlayer.start();
        for (MediaPlayerCallback callback : callbacks){
            callback.onPlayingChanged(isPlaying());
        }
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public interface MediaPlayerCallback{
        void onPlayingChanged(boolean isPlaying);
        void onMediaPlayerError();
    }

    private void initNewMediaPlayer(Context context, String uriString) throws IOException {
        if (uriString == null){

        }
        else if (uriString.isEmpty()){

        }
        else {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // If breaks, try Context.AUDIO_SERVICE or context.AUDIO_SERVICE here instead of just AUDIO_SERVICE
            mediaPlayer.setOnPreparedListener(mp -> startPlaying());
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    for (MediaPlayerCallback callback : callbacks) {
                        callback.onMediaPlayerError();
                    }
                    Log.e(TAG, "Error with URL");
                    return false;
                }
            });
            mediaPlayer.setDataSource(context, Uri.parse(uriString));
            mediaPlayer.prepareAsync();
        }
    }
}
