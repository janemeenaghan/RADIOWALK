package com.example.janecapstoneproject;
import static android.content.Context.AUDIO_SERVICE;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import java.util.ArrayList;
public class VolumeController {
    private AudioManager leftAm;
    private ArrayList<VolumeController.VolumeCallback> callbacks = new ArrayList<>();
    public VolumeController(Context context){
        leftAm = (AudioManager)context.getSystemService(AUDIO_SERVICE);
        for (VolumeController.VolumeCallback callback : callbacks){
            callback.onMaxVolumeChanged(leftAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            callback.onVolumeChanged(leftAm.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }
    public int getMaxVolume(){
        return leftAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
    public float getVolumeIncrement(){
        return (float)(leftAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/15;
    }
    public void setVolume(int volume){
        leftAm.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }
    public int getVolume(){
        return leftAm.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    public void raiseVolume(){
        float newValue = ((float)leftAm.getStreamVolume(AudioManager.STREAM_MUSIC)) + getVolumeIncrement();
        leftAm.setStreamVolume(AudioManager.STREAM_MUSIC, ((int)(newValue)),0);
    }
    public void lowerVolume(){
        float newValue = ((float)leftAm.getStreamVolume(AudioManager.STREAM_MUSIC)) - getVolumeIncrement();
        leftAm.setStreamVolume(AudioManager.STREAM_MUSIC, ((int)(newValue)),0);
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