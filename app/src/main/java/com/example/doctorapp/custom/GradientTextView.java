package com.example.doctorapp.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.example.doctorapp.R;

public class GradientTextView extends AppCompatTextView {
    private int startGradientColor = Color.BLACK;
    private int endGradientColor = Color.BLACK;

    public GradientTextView(Context context) {
        super(context, null, -1);
        init(null);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs, -1);
        init(attrs);
    }



    public GradientTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet set) {
        if (set== null)
            return;
        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.GradientTextView);
        startGradientColor = ta.getColor(R.styleable.GradientTextView_startColorGradient, Color.BLACK);
        endGradientColor = ta.getColor(R.styleable.GradientTextView_endColorGradient, Color.BLACK);
        ta.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            getPaint().setShader(getGradient());
        }
    }
    private LinearGradient getGradient(){
        return new LinearGradient(0, 0, getWidth(), getHeight(), startGradientColor,
                endGradientColor, Shader.TileMode.CLAMP);
    }
}