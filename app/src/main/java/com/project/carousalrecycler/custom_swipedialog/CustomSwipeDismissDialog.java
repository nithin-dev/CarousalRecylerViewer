package com.project.carousalrecycler.custom_swipedialog;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.carousalrecycler.carousal_recycler.Paramsclass;


public class CustomSwipeDismissDialog extends FrameLayout {

    private final GestureDetector gestureDetector;
    private final Paramsclass params;
    private View dialog;

    protected CustomSwipeDismissDialog(@NonNull Context context, Paramsclass params) {
        super(context);
        this.params = params;
        this.gestureDetector = new GestureDetector(context, flingGestureListener);
        init();
    }

    private void init() {
        setOnClickListener(overlayClickListener);
        setBackgroundColor(params.overlayColor);
        dialog = params.view;
        if (dialog == null) {
            dialog = LayoutInflater.from(getContext()).inflate(params.layoutRes, this, false);
        }
        LayoutParams layoutParams = (LayoutParams) dialog.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        } else {
            layoutParams.gravity = Gravity.CENTER;
        }
        dialog.setOnTouchListener(touchListener);
        addView(dialog, layoutParams);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            cancel();
            return true;
        }
        return false;
    }

    public CustomSwipeDismissDialog show() {
        WindowManager windowManager = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        windowManager.addView(this, layoutParams);
        return this;
    }

    public void cancel() {
        if (params.cancelListener != null) {
            params.cancelListener.onCancel(dialog);
        }
        dismiss();
    }

    public void dismiss() {
        dialog.setOnTouchListener(null);
        removeView(dialog);
        WindowManager windowManager = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.removeViewImmediate(this);
    }

    private void dismiss(SwipeDismissDirectionfinder direction) {
        if (params.swipeDismissListener != null) {
            params.swipeDismissListener.onSwipeDismiss(this, direction);
        }
        dismiss();
    }

    private final OnTouchListener touchListener = new OnTouchListener() {

        private float initCenterX;
        private float lastEventY;
        private float lastEventX;
        private float initY;
        private float initX;

        public boolean onTouch(View view, MotionEvent motionEvent) {
            /*Fling detected*/
            if (gestureDetector.onTouchEvent(motionEvent)) {
                return true;
            }

            int action = motionEvent.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    initX = view.getX();
                    initY = view.getY();
                    lastEventX = motionEvent.getRawX();
                    lastEventY = motionEvent.getRawY();
                    initCenterX = initX + view.getWidth() / 2;
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    float eventX = motionEvent.getRawX();
                    float eventY = motionEvent.getRawY();
                    float eventDx = eventX - lastEventX;
                    float eventDy = eventY - lastEventY;
                    float centerX = view.getX() + eventDx + view.getWidth() / 2;
                    float centerDx = centerX - initCenterX;
                    view.setX(view.getX() + eventDx);
                    view.setY(view.getY() + eventDy);
                    float rotationAngle = centerDx * params.horizontalOscillation / initCenterX;
                    view.setRotation(rotationAngle);
                    view.invalidate();
                    lastEventX = eventX;
                    lastEventY = eventY;
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    PropertyValuesHolder horizontalAnimation =
                            PropertyValuesHolder.ofFloat("x", initX);
                    PropertyValuesHolder verticalAnimation =
                            PropertyValuesHolder.ofFloat("y", initY);
                    PropertyValuesHolder rotateAnimation =
                            PropertyValuesHolder.ofFloat("rotation", 0f);
                    ObjectAnimator originBackAnimation =
                            ObjectAnimator.ofPropertyValuesHolder(view, horizontalAnimation,
                                    verticalAnimation, rotateAnimation);
                    originBackAnimation.setInterpolator(
                            new AccelerateDecelerateInterpolator());
                    originBackAnimation.setDuration(300);
                    originBackAnimation.start();
                    break;
                }
            }
            return true;
        }
    };

    private final OnClickListener overlayClickListener = v -> cancel();

    private final GestureDetector.SimpleOnGestureListener flingGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int maxVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
            float normalizedVelocityX = Math.abs(velocityX) / maxVelocity;
            float normalizedVelocityY = Math.abs(velocityY) / maxVelocity;
            if (normalizedVelocityX > params.flingVelocity) {
                SwipeDismissDirectionfinder direction = (e2.getRawX() > e1.getRawX())
                        ? SwipeDismissDirectionfinder.RIGHT
                        : SwipeDismissDirectionfinder.LEFT;
                dismiss(direction);
                return true;
            } else if (normalizedVelocityY > params.flingVelocity) {
                SwipeDismissDirectionfinder direction = (e2.getRawY() > e1.getRawY())
                        ? SwipeDismissDirectionfinder.BOTTOM
                        : SwipeDismissDirectionfinder.TOP;
                dismiss(direction);
                return true;
            } else {
                return false;
            }
        }
    };

    public static class Builder {

        private final Paramsclass params;
        private final Context context;

        public Builder(Context context) {
            this.context = context;
            this.params = new Paramsclass();
        }

        public Builder setView(@NonNull View view) {
            params.view = view;
            params.layoutRes = 0;
            return this;
        }

        public Builder setLayoutResId(@LayoutRes int layoutResId) {
            params.layoutRes = layoutResId;
            params.view = null;
            return this;
        }

        public Builder setFlingVelocity(@FloatRange(from = 0, to = 1.0) float flingVelocity) {
            params.flingVelocity = flingVelocity;
            return this;
        }

        public Builder setOnSwipeDismissListener(@Nullable OnSwipeDismissListener swipeDismissListener) {
            params.swipeDismissListener = swipeDismissListener;
            return this;
        }

        public Builder setOverlayColor(@ColorInt int color) {
            params.overlayColor = color;
            return this;
        }

        public Builder setHorizontalOscillation(@FloatRange(from = 0.0, to = 35.0) float oscillation) {
            params.horizontalOscillation = oscillation;
            return this;
        }

        public CustomSwipeDismissDialog build() {
            if (params.view == null && params.layoutRes == 0) {
                throw new IllegalStateException("view should be set with setView(View view) " +
                        "or with setLayoutResId(int layoutResId)");
            }
            return new CustomSwipeDismissDialog(context, params);
        }
    }
}
