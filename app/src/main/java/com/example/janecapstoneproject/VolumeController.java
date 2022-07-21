package com.example.janecapstoneproject;

import static android.content.Context.AUDIO_SERVICE;
import android.content.Context;
import android.media.AudioManager;

import java.util.ArrayList;

public class VolumeController {
    AudioManager leftAm;
    private ArrayList<VolumeController.VolumeCallback> callbacks = new ArrayList<>();

    public VolumeController(Context context){
        leftAm = (AudioManager)context.getSystemService(AUDIO_SERVICE);
        for (VolumeController.VolumeCallback callback : callbacks){
            callback.onMaxVolumeChanged(leftAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            callback.onVolumeChanged(leftAm.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }
    public void setVolume(int volume){
        leftAm.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        for (VolumeController.VolumeCallback callback : callbacks){
            callback.onVolumeChanged(leftAm.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }
    public int getVolume(){
        return leftAm.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    public void raiseVolume(){
        leftAm.adjustVolume(AudioManager.ADJUST_RAISE,0);
        for (VolumeController.VolumeCallback callback : callbacks) {
            callback.onVolumeChanged(leftAm.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }
    public void lowerVolume(){
        leftAm.adjustVolume(AudioManager.ADJUST_LOWER,0);
        for (VolumeController.VolumeCallback callback : callbacks) {
            callback.onVolumeChanged(leftAm.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }
    public void registerCallback(VolumeController.VolumeCallback volumeCallback){
        if (!callbacks.contains(volumeCallback)){
            callbacks.add(volumeCallback);
        }
    }

    public void unRegisterCallback(VolumeController.VolumeCallback callback){
        if (callbacks.contains(callback)){
            callbacks.remove(callback);
        }
    }
    public interface VolumeCallback {
        void onVolumeChanged(int volume);
        void onMaxVolumeChanged(int maxVolume);
    }
}