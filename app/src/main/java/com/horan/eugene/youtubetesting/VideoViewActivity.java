package com.horan.eugene.youtubetesting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;


public class VideoViewActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    public static final String API_KEY = "AIzaSyCoJJUPUV3tFXRDwgggHaZ5y7tc8DQwuk8";
    String VIDEO_ID = "RXSFRMJhlgY";
    YouTubePlayerView youTubePlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtubeplayerview);
        if (getIntent().getExtras() != null) {
            VIDEO_ID = getIntent().getExtras().get("VIDEO_ID").toString();
            youTubePlayerView.initialize(API_KEY, this);
            TextView title = (TextView) findViewById(R.id.title);
            title.setText(getIntent().getStringExtra("VIDEO_TITLE"));
            TextView description = (TextView) findViewById(R.id.description);
            description.setText(getIntent().getStringExtra("VIDEO_DESCRIPTION"));
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer.loadVideo(VIDEO_ID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

}
