package com.example.doctorapp.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.doctorapp.R;

public class CircleMedicView  extends View {

    private LinearGradient gradient;
    private int mColorStartGradient = Color.BLUE;
    private int mColorEndGradient = Color.RED;
    private Paint paint;
    private Path path;
    private RectF oval;
    float center_x, center_y;


    public CircleMedicView(Context context) {
        super(context);
        init(null);
    }

    public CircleMedicView(Context context,@Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CircleMedicView(Context context,@Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public CircleMedicView(Context context,@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set){
        path = new Path();
        oval = new RectF();
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (set== null)
            return;
        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.CircleMedicView);
        mColorStartGradient = ta.getColor(R.styleable.CircleMedicView_startGradient, Color.BLUE);
        mColorEndGradient = ta.getColor(R.styleable.CircleMedicView_endGradient,Color.RED);
        ta.recycle();
    }
    Paint p;
    @Override
    protected void onDraw(Canvas canvas) {

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.GRAY);
        p.setAntiAlias(true);
        p.setShader(getGradient());
        // овал по всему экрану
        if (heighSet){
            RectF oval1 = new RectF(-getWidth()/2, -heigh/2, getWidth()+getWidth()/2, heigh);
            canvas.drawOval(oval1, p);
        }else {
            RectF oval1 = new RectF(-getWidth()/2, -getHeight()/2, getWidth()+getWidth()/2, getHeight());
            canvas.drawOval(oval1, p);
        }
    }

    private Shader getGradient(){
        return new LinearGradient(
                0,
                0,
                getWidth(),
                getHeight(),
                mColorStartGradient,
                mColorEndGradient,
                Shader.TileMode.MIRROR);
    }
    private float heigh;
    private boolean heighSet = false;
    public void setHeigh(float px) {
        this.heigh = px;
        heighSet = true;
        invalidate();
    }
}
