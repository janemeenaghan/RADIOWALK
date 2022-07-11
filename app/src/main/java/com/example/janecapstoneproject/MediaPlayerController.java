package com.example.janecapstoneproject;
import static android.content.Context.AUDIO_SERVICE;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.SeekBar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;


import java.io.IOException;

public class MediaPlayerController {
    MediaPlayer mediaPlayer;
    int maxVolume, curVolume;
    AudioManager leftAm;
    private Context context;
    SeekBar volControl;

    public MediaPlayerController(Context context, SeekBar volControl){
        this.context = context;
        this.volControl = volControl;
        // If breaks, try Context.AUDIO_SERVICE or context.AUDIO_SERVICE here instead of just AUDIO_SERVICE
        leftAm = (AudioManager)context.getSystemService(AUDIO_SERVICE);
        maxVolume = leftAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volControl.setMax(maxVolume);
        curVolume = leftAm.getStreamVolume(AudioManager.STREAM_MUSIC);
        volControl.setProgress(curVolume);
    }

    public void setupVolumeControl(){
        curVolume = leftAm.getStreamVolume(AudioManager.STREAM_MUSIC);
        volControl.setProgress(curVolume);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                leftAm.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void newPlayer(String uriString) throws IOException {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(context, Uri.parse(uriString));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void newPlayer(Uri uri) throws IOException {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void startPlaying(){
        mediaPlayer.start();
    }
}
