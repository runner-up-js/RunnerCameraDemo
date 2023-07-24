package com.postcard.croporuncrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class CropImage extends AppCompatImageView {
    private Bitmap bitmap;

    public CropImage(@NonNull Context context) {
        this(context, null);
    }

    public CropImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        bitmap = bm;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }


}
