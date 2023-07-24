package com.postcard.croporuncrop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.postcard.crop.util.RotationGestureDetector;

public class GestureCropImageView extends ImageLayout {
    private static final int DOUBLE_TAP_ZOOM_DURATION = 200;

    private ScaleGestureDetector mScaleDetector;
    private RotationGestureDetector mRotateDetector;
    private GestureDetector mGestureDetector;

    private float mMidPntX, mMidPntY;

    private boolean mIsRotateEnabled = true, mIsScaleEnabled = true, mIsGestureEnabled = true;
    private float mDoubleTapScaleSteps = 0.35f;

    public GestureCropImageView(Context context) {
        this(context, null);
    }

    public GestureCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupGestureListeners();
    }

    public void setScaleEnabled(boolean scaleEnabled) {
        mIsScaleEnabled = scaleEnabled;
    }

    public boolean isScaleEnabled() {
        return mIsScaleEnabled;
    }

    public void setRotateEnabled(boolean rotateEnabled) {
        mIsRotateEnabled = rotateEnabled;
    }

    public boolean isRotateEnabled() {
        return mIsRotateEnabled;
    }

    public void setGestureEnabled(boolean gestureEnabled) {
        mIsGestureEnabled = gestureEnabled;
    }

    public boolean isGestureEnabled() {
        return mIsGestureEnabled;
    }

    public void setDoubleTapScaleSteps(int doubleTapScaleSteps) {
        mDoubleTapScaleSteps = doubleTapScaleSteps;
    }


    /**
     * If it's ACTION_DOWN event - user touches the screen and all current animation must be canceled.
     * If it's ACTION_UP event - user removed all fingers from the screen and current image position must be corrected.
     * If there are more than 2 fingers - update focal point coordinates.
     * Pass the event to the gesture detectors if those are enabled.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getPointerCount() > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2;
            mMidPntY = (event.getY(0) + event.getY(1)) / 2;
        }

        if (mIsGestureEnabled) {
            mGestureDetector.onTouchEvent(event);
        }

        if (mIsScaleEnabled) {
            mScaleDetector.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                 postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         resetTranslate();
//                         resetScale();
                     }
                 },300);
                break;
        }
        return true;
    }

    private void setupGestureListeners() {
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(), null, true);
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
//        mRotateDetector = new RotationGestureDetector(new RotateListener());
    }

    protected float getDoubleTapTargetScale() {
        return 1.0f + mDoubleTapScaleSteps;
    }

    // 放大缩小操作
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float x,y;
            float top = cropImage.getTop();
            float left = cropImage.getLeft();
            x = mMidPntX - left;
            y = mMidPntY- top;
            postScale(detector.getScaleFactor(), x, y);
            return true;
        }
    }

    // 手指移动操作
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x,y;
            float top = cropImage.getTop();
            float left = cropImage.getLeft();
            x = e.getX() - left;
            y = e.getY() - top;
            postScale(getDoubleTapTargetScale(), x, y);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            postTranslate(-distanceX, -distanceY);
            return true;
        }

    }

    //    protected float getDoubleTapTargetScale() {
//        return getCurrentScale() * (float) Math.pow(getMaxScale() / getMinScale(), 1.0f / mDoubleTapScaleSteps);
//    }
    public void postScale(float deltaScale) {
    }


}
