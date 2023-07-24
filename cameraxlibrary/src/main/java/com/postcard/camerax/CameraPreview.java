package com.postcard.camerax;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.core.internal.utils.ImageUtil;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.postcard.camerax.widget.FocusImageView;
import com.postcard.tools.ToolsFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraPreview extends FrameLayout {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Executor cameraExecutor;
    private CameraSelector cameraSelector;
    //    private ImageAnalysis imageAnalysis;
    private Camera camera;
    private Preview preview;
    private ProcessCameraProvider cameraProvider;
    public static final int OR_portrait = 0x110;
    public static final int OR_landscape = 0x111;
    private int orientation = OR_portrait;
    private double ratio;
    private OnPictureListener onPictureListener;
    private double videoWidth, videoHeight;
    private FocusImageView popupWindow;

    public int getVideoOrientation() {
        return orientation;
    }

    public void setOnPictureListener(OnPictureListener onPictureListener) {
        this.onPictureListener = onPictureListener;
    }

    public void setRatio(double baseW, double baseH) {
        ratio = baseW / baseH;
        int width = getContext().getResources().getDisplayMetrics().widthPixels;
        int height = (int) (width / ratio);
        Size size = new Size((int) width, (int) height);
        // LayoutParams frameLayout = new FrameLayout.LayoutParams(width, height);
        // frameLayout.gravity = Gravity.CENTER;
        // previewView.setLayoutParams(frameLayout);
        cameraProvider.unbindAll();
        initImageCapture(size);
        setPreviewRatio(width, height,size);
        camera = cameraProvider.bindToLifecycle((LifecycleOwner) getContext(), cameraSelector, preview, imageCapture);
    }
    
    private void setPreviewRatio(int width,  int height, Size size) {
        LayoutParams frameLayout = new FrameLayout.LayoutParams(width, height);
        frameLayout.gravity = Gravity.CENTER;
        previewView.setLayoutParams(frameLayout);
        preview = new Preview.Builder()
                // 设定预览比例输出的预览图像比例
                .setTargetResolution(size)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    public CameraPreview(Context context) {
        super(context, null);
    }

    public CameraPreview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CameraPreview);
        videoWidth = (double) typedArray.getInteger(R.styleable.CameraPreview_video_width, 1228);
        videoHeight = (double) typedArray.getInteger(R.styleable.CameraPreview_video_height, 1795);

        cameraExecutor = Executors.newSingleThreadExecutor();
        initProcessCameraProvider(context);
    }
    private void initProcessCameraProvider(Context context){
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(context));
    }
    // 初始化预览界面
    private void initPreView() {
        previewView = new PreviewView(getContext());
        addView(previewView);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
//        setGravity(Gravity.CENTER_VERTICAL);
        initListener();
    }

    // 出事化输出界面
    private void initImageCapture(Size size) {
        imageCapture = new ImageCapture.Builder()
                //优化捕获速度，可能降低图片质量
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                //设置宽高比
//                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetResolution(size)
                //设置初始的旋转角度
                .setTargetRotation(Surface.ROTATION_0)
                .build();

    }

    // 修改相机图像捕捉
    private void initImageAnalysis(Size size) {
//        imageAnalysis =
//                new ImageAnalysis.Builder()
//                        .setTargetResolution(size)
//                        .build();
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        initPreView();
        setRatio(videoWidth, videoHeight);
    }

    public void setVideoOrientation(int orientation) {
        if (previewView == null) {
            return;
        }
        this.orientation = orientation;
        double width;
        double height;
        if (orientation == OR_portrait) {
            width = videoWidth;
            height = videoHeight;
        } else {
            width = videoHeight;
            height = videoWidth;
        }
        setRatio(width, height);
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    public void changeTargetResolution() {
        double width;
        double height;
        if (orientation == OR_landscape) {
            width = videoWidth;
            height = videoHeight;
            this.orientation = OR_portrait;
        } else {
            width = videoHeight;
            height = videoWidth;
            this.orientation = OR_landscape;
        }
        setRatio(width, height);
    }

    private void initListener() {
        previewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LiveData<ZoomState> zoomState = camera.getCameraInfo().getZoomState();
                float maxZoomRatio = zoomState.getValue().getMaxZoomRatio();
                float minZoomRatio = zoomState.getValue().getMinZoomRatio();
                return new CameraXGestureDetector(getContext()) {
                    @Override
                    void zoom() {
                        float zoomRatio = zoomState.getValue().getZoomRatio();
                        if (zoomRatio < maxZoomRatio) {
                            camera.getCameraControl().setZoomRatio((float) (zoomRatio + 0.1));
                        }
                    }

                    @Override
                    void zoomOut() {
                        float zoomRatio = zoomState.getValue().getZoomRatio();
                        if (zoomRatio > minZoomRatio) {
                            camera.getCameraControl().setZoomRatio((float) (zoomRatio - 0.1));
                        }
                    }

                    @Override
                    void click(float x, float y) {
                        Log.e("click", "click");

                    }

                    @Override
                    void doubleClick(float x, float y) {
                        Log.e("doubleClick", "doubleClick");
                        float zoomRatio = zoomState.getValue().getZoomRatio();
                        if (zoomRatio > minZoomRatio) {
                            camera.getCameraControl().setLinearZoom(0f);
                        } else {
                            camera.getCameraControl().setLinearZoom(0.5f);
                        }
                    }

                    @Override
                    void longClick(float x, float y) {
                        showTapView(x, y);
                    }
                }.onTouchEvent(event);
            }
        });
    }
    public int dipToPixel(float dip) {
        return Math.round(getContext().getResources().getDisplayMetrics().density * dip);
    }
    private void showTapView(float x, float y) {
        if (popupWindow == null) {
            popupWindow = new FocusImageView(getContext());
            popupWindow.setLayoutParams(new FrameLayout.LayoutParams(dipToPixel(66), dipToPixel(66)));
            addView(popupWindow);
        }
        MeteringPointFactory factory = previewView.getMeteringPointFactory();
        MeteringPoint point = factory.createPoint(x, y);
        FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                .build();
        CameraInfo cameraInfo = camera.getCameraInfo();
        CameraControl cameraControl = camera.getCameraControl();
        if (cameraInfo.isFocusMeteringSupported(action)) {
            cameraControl.cancelFocusAndMetering();
            popupWindow.setDisappear(false);
            popupWindow.startFocus(new Point((int) x, (int) y));
        }

        ListenableFuture<FocusMeteringResult> future = cameraControl.startFocusAndMetering(action);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    FocusMeteringResult result = future.get();
                    popupWindow.setDisappear(true);
                    if (result.isFocusSuccessful()) {
                        popupWindow.onFocusSuccess();
                    } else {
                        popupWindow.onFocusFailed();
                    }
                } catch (Exception ignored) {
                }
            }
        }, cameraExecutor);
        previewView.playSoundEffect(SoundEffectConstants.CLICK);
    }


    public void takePicture() {
        File file = new File(ToolsFile.createImagePathUri(getContext()));
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(file)
                        .build();
        imageCapture.takePicture(outputFileOptions, (Executor) cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        try {
                            Uri contentUri = outputFileResults.getSavedUri();
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri));
                            } else {

                                MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                                        file.getAbsolutePath(), file.getName(), null);

                            }
                            onFileSaved(contentUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), error.getMessage() + "", Toast.LENGTH_LONG).show();
                            }
                        });
                        // insert your code here.
                        Log.e("imageCapture", "ImageCaptureException-error");
                    }
                }
        );
    }

    private void onFileSaved(Uri savedUri) {
        post(new Runnable() {
            @Override
            public void run() {
                if (onPictureListener != null) {
                    onPictureListener.onImageSaved(savedUri);
                }
            }
        });

    }

    public interface OnPictureListener {
        void onImageSaved(Uri savedUri);
    }

    @NonNull
    @SuppressLint("RestrictedApi")
    private byte[] imageToJpegByteArray(@NonNull ImageProxy image, @IntRange(from = 1,
            to = 100) int jpegQuality) throws ImageUtil.CodecFailedException {
        boolean shouldCropImage = ImageUtil.shouldCropImage(image);
        int imageFormat = image.getFormat();

        if (imageFormat == ImageFormat.JPEG) {
            if (!shouldCropImage) {
                // When cropping is unnecessary, the byte array doesn't need to be decoded and
                // re-encoded again. Therefore, jpegQuality is unnecessary in this case.
                return ImageUtil.jpegImageToJpegByteArray(image);
            } else {
                return ImageUtil.jpegImageToJpegByteArray(image, image.getCropRect(), jpegQuality);
            }
        } else if (imageFormat == ImageFormat.YUV_420_888) {
            return ImageUtil.yuvImageToJpegByteArray(image, shouldCropImage ? image.getCropRect() :
                    null, jpegQuality);
        } else {
        }

        return null;
    }
}