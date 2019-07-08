package com.example.doctorapp.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.doctorapp.R;

public class Wave extends View {

    private Paint wavePaint;
    private Path path;
    private Paint waveBlurPaint;
    private Path blurPath;
    private int mColorStartGradient = Color.RED;
    private int mColorEndGradient = Color.RED;
    private int mColorStartGradientBlur = Color.RED;
    private int mColorEndGradientBlur = Color.RED;

    private LinearGradient gradient;

    public Wave(Context context) {
        super(context);
        init(null);
    }

    public Wave(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public Wave(Context context, @Nullable  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public Wave(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init (@Nullable AttributeSet set){
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.RED);
        waveBlurPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //wavePaint.setStyle(Paint.Style.STROKE);
        //wavePaint.setStyle(Paint.Style.FILL);
        path = new Path();
        blurPath = new Path();

        if (set== null)
            return;
        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.Wave);
        mColorStartGradient = ta.getColor(R.styleable.Wave_startColor, Color.RED);
        mColorEndGradient = ta.getColor(R.styleable.Wave_endColor, Color.RED);
        mColorStartGradientBlur = ta.getColor(R.styleable.Wave_startBlurColor,Color.BLUE);
        mColorEndGradientBlur = ta.getColor(R.styleable.Wave_endBlurColor, Color.BLUE);
        ta.recycle();


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wavePaint.setStrokeWidth(3);
        path.moveTo(0, (float) (getHeight()-getHeight()*0.05));
        path.cubicTo(
                (float) (getWidth()-getWidth()*0.8),
                (float) (getHeight()-getHeight()*0.85),
                (float) (getWidth()- getWidth()*0.1),
                (float) (getHeight()-getHeight()*0.4),
                (float) (getWidth()- getWidth()*0.1), 0);
        path.lineTo(0,0);
        path.close();

        blurPath.moveTo(0, getHeight());
        blurPath.cubicTo(
                (float) (getWidth()-getWidth()*0.75),
                (float) (getHeight()-getHeight()*0.75),
                (float) (getWidth()- getWidth()*0.15),
                (float) (getHeight()-getHeight()*0.3),
                (float) (getWidth()- getWidth()*0.05), 0);
        blurPath.lineTo(0,0);
        blurPath.close();

        waveBlurPaint.setShader(getGradientBlur());
        canvas.drawPath(blurPath,waveBlurPaint);
        wavePaint.setShader(getGradient());
        canvas.drawPath(path, wavePaint);
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
    private Shader getGradientBlur(){
        return new LinearGradient(
                0,
                0,
                getWidth(),
                getHeight(),
                mColorStartGradientBlur,
                mColorEndGradientBlur,
                Shader.TileMode.MIRROR);
    }
}
