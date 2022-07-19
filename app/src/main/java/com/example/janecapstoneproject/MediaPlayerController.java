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

    public void setURLAndPrepare(String uriString) {
        if (currentURI == null || currentURI.isEmpty() || !currentURI.equals(uriString)) {
            try {
                Log.e("EEJ", uriString);
                Log.e("EEJ2", "" + context);
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
                initNewMediaPlayer(context, uriString);
            } catch (IOException e) {
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
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // If breaks, try Context.AUDIO_SERVICE or context.AUDIO_SERVICE here instead of just AUDIO_SERVICE
        mediaPlayer.setOnPreparedListener(mp -> startPlaying());
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                for (MediaPlayerCallback callback : callbacks){
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
