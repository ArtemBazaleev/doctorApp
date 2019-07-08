package com.example.doctorapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class ImageLoader {
    private String[] images;
    private Context mContext;
    private ArrayList<Bitmap> bitmaps;
    private onLoadingFinished listener;

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
        this.imageView.setTag(target);
    }

    private ImageView imageView;
    final private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            listener.onBitmapLoaded(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            listener.onFailed();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    public ImageLoader(Context ctx) {
        bitmaps = new ArrayList<>();
        mContext = ctx;

    }

    public void load(String[] images, onLoadingFinished listener){
        this.images = images;
        this.listener = listener;
        for (String s:images) {
            Picasso.with(mContext)
                    .load(s)
                    .into(imageView);
        }
        listener.finished();
    }

    public interface onLoadingFinished{
        void finished();
        void onBitmapLoaded(Bitmap btm);
        void onFailed();
    }
}
