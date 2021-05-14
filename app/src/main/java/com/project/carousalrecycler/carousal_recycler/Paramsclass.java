package com.project.carousalrecycler.carousal_recycler;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.project.carousalrecycler.custom_swipedialog.OnCancelListener;
import com.project.carousalrecycler.custom_swipedialog.OnSwipeDismissListener;

public class Paramsclass {
    public View view = null;
    @LayoutRes
    public int layoutRes = 0;
    @FloatRange(from = 0, to = 1.0)
    public float flingVelocity = 0.1f;
    @ColorInt
    public int overlayColor = Color.parseColor("#80444444");
    @Nullable
    public OnSwipeDismissListener swipeDismissListener;
    @Nullable
    public OnCancelListener cancelListener;
    @FloatRange(from = 0.0, to = 35.0)
    public float horizontalOscillation = 35.0f;
}
