package com.horan.eugene.youtubetesting.UI;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.horan.eugene.youtubetesting.Utilities.Constants;
import com.horan.eugene.youtubetesting.R;


public class VideoViewActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    String VIDEO_ID = "RXSFRMJhlgY";
    YouTubePlayerView youTubePlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtubeplayerview);
        if (getIntent().getExtras() != null) {
            VIDEO_ID = getIntent().getExtras().get("VIDEO_ID").toString();
            youTubePlayerView.initialize(Constants.API_KEY, this);
            TextView title = (TextView) findViewById(R.id.title);
            title.setText(getIntent().getStringExtra("VIDEO_TITLE"));
            Log.e("FROMCLASS", getIntent().getExtras().get("VIDEO_ID").toString());
            TextView description = (TextView) findViewById(R.id.description);
            description.setText(getIntent().getStringExtra("VIDEO_DESCRIPTION"));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    YouTubePlayer youTubePlayer1;

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer1 = youTubePlayer;
            youTubePlayer.loadVideo(VIDEO_ID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}
