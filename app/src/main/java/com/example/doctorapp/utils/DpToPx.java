package com.example.doctorapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class DpToPx {
    public static float dpToPx(float dp, Context context){
        Resources r = context.getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }
}
