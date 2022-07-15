package com.example.janecapstoneproject;

import static android.content.Context.AUDIO_SERVICE;

import android.content.Context;
import android.media.AudioManager;

public class VolumeController {
    AudioManager leftAm;

    public VolumeController(Context context, VolumeCallback callback){
        leftAm = (AudioManager)context.getSystemService(AUDIO_SERVICE);
        callback.onMaxVolumeChanged(leftAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        callback.onVolumeChanged(leftAm.getStreamVolume(AudioManager.STREAM_MUSIC));
    }
    public interface VolumeCallback {
        void onVolumeChanged(int volume);
        void onMaxVolumeChanged(int maxVolume);
    }
    public void setVolume(int volume){
        leftAm.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }
}
