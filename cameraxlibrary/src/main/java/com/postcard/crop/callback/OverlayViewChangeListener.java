package com.postcard.crop.callback;

import android.graphics.RectF;

/**
 * Created by Oleksii Shliama.
 */
public interface OverlayViewChangeListener {

    void onCropRectUpdated(RectF cropRect);

    void postTranslate(float deltaX, float deltaY);
}