package com.postcard.croporuncrop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.postcard.camerax.R;

// 图片处理基本类
public class ImageLayout extends FrameLayout {
    private boolean isMirror = false, isCrop = false;
    public CropImage cropImage; // 图片展示
    private OverlayView overlayView; // 覆盖框
    private Bitmap cacheBitmap, opterBitmap;
    private float rotation = 0f, deltaScale = 1f, translateX, translateY, minScale = 1f;
    private Matrix matrix;
    private boolean openCut = false;
    private boolean isFill = false;
    public  boolean isEdit = false;

    public ImageLayout(@NonNull Context context) {
        this(context, null);
    }

    public ImageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        TypedArray theme = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView);
        overlayView.processStyledAttributes(theme);
        theme.recycle();
    }



    public float getCurrentScale() {
        return deltaScale;
    }

    private void initView(Context context) {
        cropImage = new CropImage(context);
        overlayView = new OverlayView(context);
        cropImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        overlayView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setPadding(dipToPixel(16), dipToPixel(16), dipToPixel(16), dipToPixel(16));
        addView(cropImage);
        addView(overlayView);
        matrix = new Matrix();
        overlayView.setVisibility(INVISIBLE);
        isFill = true;
    }

    public void fill() {
        isFill = true;
        openCut = false;
        RectF rectF = overlayView.getCropViewRect();
        float top = (int) rectF.top;
        float left = (int) rectF.left;
        float width = (int) (rectF.right - left);
        float height = (int) (rectF.bottom - top);
        LayoutParams layoutParams = new LayoutParams((int) width, (int) height);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(dipToPixel(16), dipToPixel(16), dipToPixel(16), dipToPixel(16));
        cropImage.setLayoutParams(layoutParams);
        cropImage.setBackgroundColor(Color.WHITE);
        recoverScale();
        recoverTranslate();
        isEdit = true;
    }
    public void initLayout(){
//        RectF rectF = overlayView.getCropViewRect();
//        float top = (int) rectF.top;
//        float left = (int) rectF.left;
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float width = dm.widthPixels;
        float ratio = overlayView.getTargetAspectRatio();
//        float width = getMeasuredWidth() - dipToPixel(32);
        float height = width / ratio;
        Log.e("initLayout",width +":" + height+":" + ratio);
        LayoutParams layoutParams = new LayoutParams((int) width, (int) height);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(dipToPixel(16), dipToPixel(16), dipToPixel(16), dipToPixel(16));
        cropImage.setLayoutParams(layoutParams);
        cropImage.setBackgroundColor(Color.WHITE);
//        recoverScale();
//        recoverTranslate();
    }
    public void setTargetAspectRatio(float ratio) {
        overlayView.setTargetAspectRatio(ratio);
        initLayout();
    }

    public void setImageBitmap(Bitmap bitmap) {
        overlayView.post(new Runnable() {
            @Override
            public void run() {
                cacheBitmap = bitmap;
//                initCropView();
                float[] size = getRectSize();
                float width = size[0];
                float height = size[1];
                float bitmapWidth = cacheBitmap.getWidth();
                float bitmapHeight = cacheBitmap.getHeight();
                float sizeRatio = width / height;
                float realWidth, realHeight;
                float bitmapSizeRatio = bitmapWidth / bitmapHeight;

                if (bitmapWidth / sizeRatio < height) {
                    realHeight = width / bitmapSizeRatio;
                    realWidth = width;
                } else {
                    realHeight = height;
                    realWidth = height * bitmapSizeRatio;
                }
                float scale_w = ((float) realWidth) / bitmapWidth;
                float scale_h = ((float) realHeight) / bitmapHeight;
                Matrix matrix = new Matrix();
                matrix.postScale(scale_w, scale_h);
                // 得到新的图片
                cacheBitmap = Bitmap.createBitmap(cacheBitmap, 0, 0, (int) bitmapWidth, (int) bitmapHeight, matrix, true);
                cropImage.setImageBitmap(cacheBitmap);
                isEdit = false;
            }
        });
    }
    public int dipToPixel(float dip) {
        return Math.round(getContext().getResources().getDisplayMetrics().density * dip);
    }
    private void initCropView() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(dipToPixel(16), dipToPixel(16), dipToPixel(16), dipToPixel(16));
        cropImage.setLayoutParams(layoutParams);
    }

    private float[] getRectSize() {
        RectF rectF = overlayView.getCropViewRect();
        float top = (int) rectF.top;
        float left = (int) rectF.left;
        float width = (int) (rectF.right - left);
        float height = (int) (rectF.bottom - top);
        return new float[]{width, height};
    }

    public void reduction() {
        setImageBitmap(cacheBitmap);
        cropImage.setBackgroundColor(Color.TRANSPARENT);
        if (isCrop && isMirror) {
            isMirror = false;
            cropImage.setRotationY(1);
            cropImage.setRotation(0);
            rotation = 0;
        }
        if (!isCrop) {
            AnimatorSet translationAnimatorSet = new AnimatorSet();
            ObjectAnimator invisToVis = ObjectAnimator.ofFloat(cropImage, "rotationY",
                    180f, 0f);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(cropImage, "rotation", rotation, 0);
            if (isMirror) {
                translationAnimatorSet.playTogether(invisToVis, rotate);
                isMirror = false;
            } else {
                translationAnimatorSet.playTogether(rotate);
            }
            translationAnimatorSet.setDuration(400);
            translationAnimatorSet.start();
            rotation = 0;
        }
        recoverScale();
        isEdit = false;
    }

    // 开启剪切
    public void open() {
        if (isFill) {
            initCropView();
            cropImage.post(new Runnable() {
                @Override
                public void run() {
                    setCut();
                }
            });
            isFill = false;
        } else {
            initCropView();
            cropImage.post(new Runnable() {
                @Override
                public void run() {
                    setCut();
                }
            });
        }
    }

    public void setCut() {
        openCut = true;
        overlayView.setVisibility(VISIBLE);
        resetScale();
    }

    public void resetScale() {
        RectF rectF = overlayView.getCropViewRect();
        float top = (int) rectF.top;
        float left = (int) rectF.left;
        float width = (int) (rectF.right - left);
        float height = (int) (rectF.bottom - top);
        float bitMapw = cropImage.getBitmap().getWidth();
        float cropW = cropImage.getWidth();
        float radio = cropW / bitMapw;
        float cropH = (int) (cropImage.getHeight());
        cropImage.setBackgroundColor(Color.TRANSPARENT);
        if (cropH < height) {
            postScale(height / cropH, cropW / 2, cropH / 2);
        }
        if (cropW < width) {
            postScale(width / cropW, cropW / 2, cropH / 2);
        }
        minScale = deltaScale;
    }

    // 关闭剪切
    public void close() {
        initLayout();
        cropImage.post(new Runnable() {
            @Override
            public void run() {
                if (isFill) {
                    cropImage.setBackgroundColor(Color.TRANSPARENT);
                    isFill = false;
                }
                openCut = false;
                overlayView.setVisibility(INVISIBLE);
                recoverScale();
                recoverTranslate();
                cropImage.setBackgroundColor(Color.WHITE);
            }
        });

    }

    public void complete() {
        openCut = false;
        overlayView.setVisibility(INVISIBLE);
        RectF rectF = overlayView.getCropViewRect();
        int top = (int) rectF.top;
        int left = (int) rectF.left;
        int width = (int) (rectF.right - rectF.left);
        int height = (int) (rectF.bottom - rectF.top);
        Bitmap bm = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        draw(new Canvas(bm));
        Bitmap imgBitmap = Bitmap.createBitmap(bm, left, top, width, height);
        cropImage.setImageBitmap(imgBitmap);
        cropImage.setPivotY(cropImage.getHeight() / 2);
        cropImage.setPivotX(cropImage.getWidth() / 2);
        cropImage.setRotation(0);
        cropImage.setScaleX(1);
        cropImage.setScaleY(1);
        cropImage.setRotationY(0);
        cropImage.setTranslationX(1f);
        cropImage.setTranslationY(1f);
        this.translateX = 1f;
        this.translateY = 1f;
        this.deltaScale = 1f;
        this.rotation = 0;
    }

    //    public void
    // 镜像
    public void postMirror() {
//        cropImage.setPivotX(cropImage.getWidth() / 2);
//        cropImage.setPivotY(cropImage.getHeight() / 2);
        ObjectAnimator invisToVis = ObjectAnimator.ofFloat(cropImage, "rotationY",
                0f, 180f);
        if (isMirror) {
            invisToVis = ObjectAnimator.ofFloat(cropImage, "rotationY",
                    180f, 0f);
            isMirror = false;
        } else {
            isMirror = true;
        }
        invisToVis.setDuration(500);
        invisToVis.setInterpolator(new DecelerateInterpolator());
        invisToVis.start();
        isEdit = true;
    }

    /**
     * 旋转
     *
     * @param rotation
     */
    public void rotation(float rotation) {
        float newRotation = this.rotation + rotation;
        cropImage.setPivotX(cropImage.getWidth() / 2);
        cropImage.setPivotY(cropImage.getHeight() / 2);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(cropImage, "rotation", this.rotation, newRotation).setDuration(200);
        rotate.setInterpolator(new BounceInterpolator());
        rotate.start();
        this.rotation = newRotation;
        isEdit = true;
    }

    public void postScale(float deltaScale, float potX, float potY) {
        float newDeltaScale = this.deltaScale * deltaScale;
        Log.e("postScale", minScale+":" + newDeltaScale);
        if (!openCut) {
            return;
        }
        if (minScale > newDeltaScale) {
            return;
        }

        AnimatorSet animationSet = new AnimatorSet();
        cropImage.setPivotX(potX);
        cropImage.setPivotY(potY);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cropImage, "scaleY", this.deltaScale, newDeltaScale);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cropImage, "scaleX", this.deltaScale, newDeltaScale);
        animationSet.playTogether(scaleX, scaleY);
        animationSet.start();
        this.deltaScale = newDeltaScale;
        isEdit = true;
    }

    public void postTranslate(float x, float y) {
        if (!openCut) {
            return;
        }
        float newTranslateX = this.translateX + x;
        float newTranslateY = this.translateY + y;
        AnimatorSet animationSet = new AnimatorSet();
        ObjectAnimator translateX = ObjectAnimator.ofFloat(cropImage, "translationX", this.translateX, newTranslateX);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(cropImage, "translationY", this.translateY, newTranslateY);
        animationSet.playTogether(translateX, translateY);
        animationSet.start();
        this.translateX = newTranslateX;
        this.translateY = newTranslateY;
        isEdit = true;
    }

    public void resetTranslate() {
        Rect globalRect = new Rect();
        cropImage.getGlobalVisibleRect(globalRect);
        float cR = globalRect.right;
        float cL = globalRect.left;
        float cT = globalRect.top;
        float cB = globalRect.bottom;
        RectF rectF = overlayView.getCropViewRect();
        int[] location = new int[2];
        overlayView.getLocationOnScreen(location);
        float top = location[1] + rectF.top;
        float bottom = rectF.bottom - rectF.top + top;
        float overlayR = rectF.right;
        float overlayL = rectF.left;
        float overlayT = top;
        float overlayB = bottom;
        float moveY = 0;
        float moveX = 0;
        if (overlayR > cR) {
            moveX = overlayR - cR;
        }
        if (overlayL < cL) {
            moveX = overlayL - cL;
        }
        if (overlayT < cT) {
            moveY = overlayT - cT;
        }
        if (overlayB > cB) {
            moveY = overlayB - cB;
        }
        postTranslate(moveX, moveY);
        Log.e("onAnimationEnd", cT + "::" + top);
    }

    private void recoverTranslate() {
        AnimatorSet animationSet = new AnimatorSet();
        ObjectAnimator translateX = ObjectAnimator.ofFloat(cropImage, "translationX", this.translateX, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(cropImage, "translationY", this.translateY, 1f);
        animationSet.playTogether(translateX, translateY);
        animationSet.start();
        this.translateX = 1f;
        this.translateY = 1f;
    }

    private void recoverScale() {
        AnimatorSet animationSet = new AnimatorSet();
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cropImage, "scaleY", this.deltaScale, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cropImage, "scaleX", this.deltaScale, 1f);
        animationSet.playTogether(scaleX, scaleY);
        animationSet.start();
        animationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cropImage.setPivotX(cropImage.getWidth() / 2);
                cropImage.setPivotY(cropImage.getHeight() / 2);
            }
        });
        this.deltaScale = 1f;
    }

    public void cancelMirror() {
        if (!isMirror) {
            return;
        }
        postMirror();
    }

    public void cancelrotation() {
        rotation(-rotation);
        isEdit = false;
    }

    public Bitmap createMirrorBitmap(Bitmap bitmap) {
        if (isMirror) {
            matrix.postScale(-1, 1);   //镜像水平翻转
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        }
        return bitmap;
    }

    public Bitmap createRotation(Bitmap bitmap) {
        matrix.postRotate(rotation);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }

    public Bitmap getBitmap() {
        RectF rectF = overlayView.getCropViewRect();
        int top = (int) rectF.top;
        int left = (int) rectF.left;
        int width = (int) (rectF.right - rectF.left);
        int height = (int) (rectF.bottom - rectF.top);
        Bitmap bm = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        draw(new Canvas(bm));
        Bitmap imgBitmap = Bitmap.createBitmap(bm, left, top, width, height);
        return imgBitmap;
    }
}
