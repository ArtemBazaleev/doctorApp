package com.example.doctorapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.doctorapp.R;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.indicator.ProgressIndicator;
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.GlideImageViewFactory;
import com.github.piasy.biv.view.ImageSaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.ORIENTATION_USE_EXIF;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ResultViewActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = (view, motionEvent) -> {
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
        return false;
    };

    @BindView(R.id.bigImage) BigImageView bigImageView;
    @BindView(R.id.save) Button saveBtn;
    @BindView(R.id.share_btn) Button shareBtn;
    @BindView(R.id.rotateBtn) Button rotateBtn;
    @BindView(R.id.fullscreen_content) ConstraintLayout constraintContent;
    private float rotation = 0;
    private String photoUrl;
    public static final String IMAGE_PARAM = "IMAGE_PARAMS";

    private ImageSaveCallback callbackSave = new ImageSaveCallback() {
        @Override
        public void onSuccess(String uri) {
            Toast.makeText(ResultViewActivity.this,
                    "Добавлено в галерею",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFail(Throwable t) {
            t.printStackTrace();
            Toast.makeText(ResultViewActivity.this,
                    "Fail",
                    Toast.LENGTH_SHORT).show();
        }
    };

    private ImageSaveCallback callbackShare = new ImageSaveCallback() {
        @Override
        public void onSuccess(String uri) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
            startActivity(Intent.createChooser(share, "Share Image"));
        }

        @Override
        public void onFail(Throwable t) {
            t.printStackTrace();
            Toast.makeText(ResultViewActivity.this,
                    "Fail",
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BigImageViewer.initialize(GlideImageLoader.with(getApplicationContext()));
        setContentView(R.layout.activity_result_view);
        ButterKnife.bind(this);
        Bundle extra = getIntent().getExtras();
        if (extra != null)
            photoUrl = extra.getString(IMAGE_PARAM);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.bigImage);

        bigImageView.setProgressIndicator(new ProgressPieIndicator());
        if (photoUrl== null)
            bigImageView.showImage(Uri.parse("https://media.giphy.com/media/SwcPjkbLiF98Q/giphy.gif"));
        else bigImageView.showImage(Uri.parse(photoUrl));
        bigImageView.setTapToRetry(true);
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(view -> toggle());
        saveBtn.setOnClickListener(l -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
            try {
                bigImageView.saveImageIntoGallery();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "Ошибка сохранения фотографии", Toast.LENGTH_SHORT).show();
            }
        });

        shareBtn.setOnClickListener(l->{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
            try {
                bigImageView.setImageSaveCallback(callbackShare);
                bigImageView.saveImageIntoGallery();
                bigImageView.setImageSaveCallback(callbackSave);
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "Ошибка ", Toast.LENGTH_SHORT).show();
            }
        });

        bigImageView.setImageViewFactory(new GlideImageViewFactory());
        bigImageView.setImageSaveCallback(callbackSave);

        Button b = findViewById(R.id.dummy_button);
        b.setOnClickListener(l-> finish());



        rotateBtn.setOnClickListener(l-> { //changing orientation
            int w = constraintContent.getWidth();
            int h = constraintContent.getHeight();
            constraintContent.setRotation(rotation+=90);
            ViewGroup.LayoutParams lp = constraintContent.getLayoutParams();
            lp.height = w;
            lp.width = h;
            constraintContent.requestLayout();
        });

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int w = constraintContent.getWidth();
        int h = constraintContent.getHeight();
        //constraintContent.setRotation(rotation+=90);
        ViewGroup.LayoutParams lp = constraintContent.getLayoutParams();
        lp.height = w;
        lp.width = h;
        constraintContent.requestLayout();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
