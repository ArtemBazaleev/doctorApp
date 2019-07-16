package com.example.doctorapp.ui;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.doctorapp.R;
//import com.halilibo.bettervideoplayer.BetterVideoPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoViewActivity extends AppCompatActivity {

    //@BindView(R.id.player) BetterVideoPlayer player;
    public static final String VIDEO =  "VIDEO_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        ButterKnife.bind(this);
//        if (getIntent().getExtras()!= null)
//            player.setSource(Uri.parse(getIntent().getExtras().getString(VIDEO)));
//        else player.setSource(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));
//        player.enableSwipeGestures();
//        player.setOnClickListener(l-> hideSystemUI());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //player.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //player.start();
    }
}
