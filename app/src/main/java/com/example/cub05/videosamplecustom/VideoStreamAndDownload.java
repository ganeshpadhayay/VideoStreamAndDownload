package com.example.cub05.videosamplecustom;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by cub05 on 5/3/2018.
 */

public class VideoStreamAndDownload implements LocalFileStreamingServer.LocalFileStreamingServerCallBacks {


    private VideoView videoView;
    private MediaController mediaController;
    private int stopPosition;
    private ProgressBarCallbacks progressBarCallbacks;

    private LocalFileStreamingServer server;

    private boolean playState = false;
    private int playTime = 0;

    private Activity activity;


    public VideoStreamAndDownload(MediaController mediaController, VideoView videoView, Activity activity) {
        this.mediaController = mediaController;
        this.videoView = videoView;
        this.activity = activity;
        this.progressBarCallbacks = (ProgressBarCallbacks) activity;
    }

    public void onCreate(File file, String pathToSaveVideo, String videoUrl) {
        startServer(activity, videoUrl, pathToSaveVideo, "127.0.0.1", file);


    }

    public void startServer(final Activity activity, String videoUrl, String pathToSaveVideo, final String ipOfServer, File file) {


        Log.d("sachin", "file size " + file.length());
        server = new LocalFileStreamingServer(file, activity, VideoStreamAndDownload.this, videoUrl, pathToSaveVideo, String.valueOf(file.length()));
        server.setSupportPlayWhileDownloading(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                server.init(ipOfServer);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        server.start();

                        Log.d("sachin server url", server.getFileUrl());

                        videoView.setMediaController(mediaController);
                        videoView.setKeepScreenOn(true);
                        videoView.setVideoPath(server.getFileUrl());
                        videoView.start();
                        videoView.requestFocus();
                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                playVideo();
                                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                                    @Override
                                    public void onSeekComplete(MediaPlayer mp) {
//                                        if (playState) {
                                        videoView.start();
//                                        }


                                    }
                                });
                            }
                        });
//                        videoView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//                            @Override
//                            public void onViewAttachedToWindow(View v) {
//                                Toast.makeText(activity, "onViewAttachedToWindow", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onViewDetachedFromWindow(View v) {
//                                Toast.makeText(activity, "onViewDetachedFromWindow", Toast.LENGTH_SHORT).show();
//                            }
//                        });
                    }
                });
            }
        }).start();

    }


    @Override
    public void pauseVideo() {
//        Log.e("sachin", "paused");
        progressBarCallbacks.startProgressbar();
        try {
            videoView.pause();
            stopPosition = videoView.getCurrentPosition();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            stopPosition = 0;
        }
    }

    @Override
    public void playVideo() {
        Log.e("sachin", "resumed");
        progressBarCallbacks.stopProgressbar();
        videoView.seekTo(stopPosition);
    }

    public void stopServer() {
        server.stop();
        server.stopVideoDownloading();
    }

    public void saveInstanceState(Bundle outState) {
        Log.e("test", "play state is : " + videoView.isPlaying() + " play time is : " + videoView.getCurrentPosition());
        outState.putBoolean("play_state", videoView.isPlaying());
        outState.putInt("play_time", videoView.getCurrentPosition());
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        playState = savedInstanceState.getBoolean("play_state");
        playTime = savedInstanceState.getInt("play_time");
        stopPosition = playTime;
        Log.e("test", "play state is : " + playState + " play time is : " + playTime);
        videoView.seekTo(playTime);
    }


    public interface ProgressBarCallbacks {
        void stopProgressbar();

        void startProgressbar();

    }
}
