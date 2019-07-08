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

public class WaveProfile extends View {
    private Paint wavePaint;
    private Path path;
    private Paint waveBlurPaint;
    private Path blurPath;
    private int mColorStartGradient = Color.RED;
    private int mColorEndGradient = Color.RED;
    private int mColorStartGradientBlur = Color.RED;
    private int mColorEndGradientBlur = Color.RED;

    private LinearGradient gradient;

    public WaveProfile(Context context) {
        super(context);
        init(null);
    }

    public WaveProfile(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WaveProfile(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public WaveProfile(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.WaveProfile);
        mColorStartGradient = ta.getColor(R.styleable.WaveProfile_gradientStart, Color.RED);
        mColorEndGradient = ta.getColor(R.styleable.WaveProfile_gradientEnd, Color.RED);
        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wavePaint.setStrokeWidth(3);
        path.moveTo(0, (float) (getHeight()-getHeight()*0.05));
        path.cubicTo(
                (float) (getWidth()-getWidth()*0.92),
                (float) (getHeight()-getHeight()*0.7),
                (float) (getWidth()- getWidth()*0.0),
                (float) (getHeight()-getHeight()*0.2),
                (float) (getWidth()),
                (float) (getHeight()*0.4));
        path.lineTo(getWidth(),0);
        path.lineTo(0,0);
        path.close();
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
}
