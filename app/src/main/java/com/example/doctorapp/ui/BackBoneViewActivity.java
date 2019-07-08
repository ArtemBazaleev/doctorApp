package com.example.doctorapp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.example.doctorapp.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BackBoneViewActivity extends MvpAppCompatActivity  {
    @BindView(R.id.backBoneImage) ConstraintLayout backBone;
    @BindView(R.id.txt) TextView tv;
    @BindView(R.id.image) ImageView imageView;

    public static final String BACKBONE_ARRAY = "backbonearray";

    @BindView(R.id.progressBar) ProgressBar progressBar;
    float x;
    float y;
    String sDown;
    String sMove;
    String sUp;
    int status = 0;
    private String[] imageUrls = {"https://images.pexels.com/photos/132037/pexels-photo-132037.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
            "https://images.unsplash.com/photo-1469827160215-9d29e96e72f4?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&w=1000&q=80",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRh4e9PMzhqQJQ8A9THzgiNa108JrQAOwDtmv8LBpcJJTA9jk3Q8w",
            "https://www.adorama.com/alc/wp-content/uploads/2018/11/shutterstock_100419445-825x465.jpg"};

    private ArrayList<Bitmap> bitmaps;
    private OkHttpClient okHttpClient;
    private AlertDialog alertDialog;


    private int screenWidth = 0;
    int parts;
    int currentIndex;
    int previous;
    int btm = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_bone_view);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null) {
            imageUrls = getIntent().getExtras().getStringArray(BACKBONE_ARRAY);
        }
        bitmaps = new ArrayList<>();
        okHttpClient = new OkHttpClient();
        progressBar.setProgress(100/imageUrls.length);
        load(status);
    }

    public void showToastyMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void loadBitmaps(ArrayList<Bitmap> bitmaps) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;

        parts = screenWidth/ bitmaps.size();
        backBone.setOnTouchListener((v, event) -> {

            x = event.getX();
            y = event.getY();
            parts = screenWidth/ bitmaps.size();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: // нажатие
                    sDown = "Down: " + x + "," + y;
                    sMove = ""; sUp = "";
                    currentIndex = (int) x/parts;
                    previous = (int) x/parts;
                    break;
                case MotionEvent.ACTION_MOVE: // движение
                    sMove = "Move: " + x + "," + y;
                    Log.d("onCreate: ", sMove);
                    break;
                case MotionEvent.ACTION_UP: // отпускание
                case MotionEvent.ACTION_CANCEL:
                    sMove = "";
                    sUp = "Up: " + x + "," + y;
                    break;
            }
            tv.setText(sDown + "\n" + sMove + "\n" + sUp);
            Log.d("onCreate: ", ""+(int)x/parts);
            currentIndex = (int) x/parts;
            if (currentIndex > previous) {
                btm++;
                if (btm > bitmaps.size()-1)
                    btm = 0;
            }
            else if (currentIndex < previous)  {
                btm--;
                if (btm<0)
                    btm = bitmaps.size()-1;
            }
            else {
                previous = currentIndex;
                return true;
            }
            previous = currentIndex;
            try {
                imageView.setImageBitmap(bitmaps.get(btm));
            }catch (IndexOutOfBoundsException e){
                imageView.setImageBitmap(bitmaps.get(bitmaps.size()-1));
            }

            return true;
        });
    }


    private void load(int i){
        final Request request = new Request.Builder().url(imageUrls[i]).build();
        Log.d( "load: ", "Called");
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->{
                    AlertDialog.Builder builder = new AlertDialog.Builder(BackBoneViewActivity.this);
                    builder.setTitle("Ошибка загрузки!")
                            .setMessage("Повторить загрузку ?")
                            .setIcon(R.mipmap.ic_launcher)
                            .setCancelable(true)
                            .setNegativeButton("Нет",(dialog, id) -> BackBoneViewActivity.this.finish())
                            .setPositiveButton("Да",
                                    (dialog, id) -> {
                                        status = 0;
                                        load(status);
                                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()){
                    final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    // Remember to set the bitmap in the main thread.
                    new Handler(Looper.getMainLooper()).post(() ->{
                        bitmaps.add(bitmap);
                        onLoadNext();
                    });
                }else {
                    //Handle the error
                }
            }
        });
    }

    private void onLoadNext() {
        if (status == imageUrls.length-1) {
            progressBar.setVisibility(View.GONE);
            loadBitmaps(bitmaps);
            imageView.setImageBitmap(bitmaps.get(0));
        }
        else {
            status++;
            progressBar.setProgress((100 / imageUrls.length) * status);
            load(status);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog!=null)
            alertDialog.dismiss();
    }
}