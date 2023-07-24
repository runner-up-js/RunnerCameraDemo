package com.postcard.camerax.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;

import com.postcard.camerax.R;

public class FocusImageView extends AppCompatImageView {
    private static final long DELAY_MILLIS = 1000;
    private int mFocusImg;
    private int mFocusSucceedImg;
    private int mFocusFailedImg;
    private Animation mAnimation;
    private Handler mHandler;
    private volatile boolean isDisappear;

    public FocusImageView(Context context) {
        super(context);
        mFocusImg = R.mipmap.focus_focusing;
        mFocusSucceedImg = R.mipmap.focus_focused;
        mFocusFailedImg = R.mipmap.focus_failed;
        init();
    }

    public FocusImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FocusImageView);
        mFocusImg = typedArray.getResourceId(R.styleable.FocusImageView_focus_focusing, R.mipmap.focus_focusing);
        mFocusSucceedImg = typedArray.getResourceId(R.styleable.FocusImageView_focus_success, R.mipmap.focus_focused);
        mFocusFailedImg = typedArray.getResourceId(R.styleable.FocusImageView_focus_error, R.mipmap.focus_failed);
        typedArray.recycle();
    }

    private void init() {
        setVisibility(View.GONE);
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.focusview_show);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setDisappear(boolean disappear) {
        isDisappear = disappear;
    }

    public void startFocus(Point point) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        params.topMargin = point.y - getMeasuredHeight() / 2;
        params.leftMargin = point.x - getMeasuredWidth() / 2;
        setLayoutParams(params);
        setVisibility(View.VISIBLE);
        setFocusResource(mFocusImg);
        startAnimation(mAnimation);
    }

    public void onFocusSuccess() {
        if (isDisappear) {
            setFocusResource(mFocusSucceedImg);
        }
        mHandler.removeCallbacks(null, null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setFocusGone();
            }
        }, DELAY_MILLIS);
    }

    public void onFocusFailed() {
        if (isDisappear) {
            setFocusResource(mFocusFailedImg);
        }
        mHandler.removeCallbacks(null, null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setFocusGone();
            }
        }, DELAY_MILLIS);
    }

    private void setFocusResource(@DrawableRes int resId) {
        setImageResource(resId);
    }

    private void setFocusGone() {
        if (isDisappear) {
            setVisibility(View.GONE);
        }
    }

    public void destroy() {
        mHandler.removeCallbacks(null, null);
        setVisibility(View.GONE);
    }
}
