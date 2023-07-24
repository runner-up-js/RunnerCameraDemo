package com.example.runnercamerademo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import java.io.Serializable;

public abstract class PostCardActivity extends AppCompatActivity {
    public enum AnimType {
        ANIM_NONE//没有跳转动画
        , ANIM_LEFT_TO_RIGHT//从左到右滑动的动画
        , ANIM_RIGHT_TO_LEFT//从右到左的滑动动画
    }

    public ActivityResultLauncher<Intent> someActivityResultLauncher;
    /**
     * 返回键按下时间
     */
    private long onBackPressedTime;
    /**
     * 按返回键退出程序的按键时间间隔
     */
    private final int onBackDelayTime = 2000;

    /**
     * 序列化实体类
     */
    public final String SERIALIZA_MODEL = "serializaModel";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        int layoutId = getLayoutId();
        if (layoutId != 0) {
            setContentView(layoutId);
        } else {
            View view = getLayoutView();
            if (view != null) {
                setContentView(view);
            } else {
                throw new IllegalStateException("layoutId can not be 0");
            }
        }
        initView();
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request code
                            Intent data = result.getData();
                            PostCardActivity.this.onActivityResult(data);
                        }
                    }
                });
    }

    public void startActivityForResult(Intent intent) {
        intent.putExtra("getResult", true);
        someActivityResultLauncher.launch(intent);
    }

    protected void onActivityResult(Intent data) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static int taskId = -1;

    public static void setTaskId(int taskId) {
        PostCardActivity.taskId = taskId;
    }

    public static void consumerTask() {
        taskId = -1;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 设置布局文件ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 设置布局文件ID
     *
     * @return
     */
    protected abstract View getLayoutView();

    /**
     * 初始化视图
     */
    protected abstract void initView() ;

    /**
     * activity跳转
     *
     * @param targetClass 目标activity的class
     * @param isFinish    是否结束当前activity
     * @param animType    跳转时要执行的动画效果
     *                    ANIM_NO_ANIM：没有动画.
     *                    ANIM_LEFT_TO_RIGHT：从左到右滑动的动画
     *                    ANIM_RIGHT_TO_LEFT：从右到左的滑动动画
     */
    public void startActivity(Class<?> targetClass, boolean isFinish, AnimType animType) {
        Intent intent = new Intent();
        intent.setClass(this, targetClass);
        startActivity(intent, isFinish, animType);
    }

    /**
     * activity跳转并传递对象
     *
     * @param targetClass    目标activity的class
     * @param serializaModel 要传递的对象必须实现序列化
     * @param isFinish       是否结束当前activity.
     * @param animType       跳转时要执行的动画效果 ANIM_NO_ANIM：没有动画.
     */
    public void startActivity(Class<?> targetClass, Serializable serializaModel, boolean isFinish, AnimType animType) {
        Intent intent = new Intent();
        intent.putExtra(SERIALIZA_MODEL, serializaModel);
        intent.setClass(this, targetClass);
        startActivity(intent, isFinish, animType);
    }

    /**
     * 通过Intent启动一个activity
     *
     * @param intent   intent对象
     * @param isFinish 是否结束当前activity
     * @param animType 跳转时要执行的动画效果
     */
    public void startActivity(Intent intent, boolean isFinish, AnimType animType) {
        //启动activity
        startActivity(intent);
        //是否结束当前activity
        if (isFinish) {
            finish();
        }
    }

    /**
     * 获取上级activity传递的对象
     *
     * @return
     */
    public Serializable getSerializaModel() {
        Serializable serializaModel = null;
        try {
            serializaModel = (Serializable) getIntent().getSerializableExtra(SERIALIZA_MODEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serializaModel;
    }

    /**
     * 连续两次点击返回键退出程序
     */
    public final boolean pressAgainToExit() {
        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis - onBackPressedTime) >= onBackDelayTime) {
            onBackPressedTime = currentTimeMillis;
        } else {
            return true;
        }
        return false;
    }

    @Override
    public Resources getResources() {
        //修改系统字体大小后  程序中文字不随系统字体大小改变
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
    }
    public void onBackPressed(View view) {
        this.onBackPressed();
    }
    @Override
    public void finish() {
        super.finish();
        System.gc();
    }

    protected boolean checkSelfPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean shouldShowRequestPermissionRationale(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (shouldShowRequestPermissionRationale(permissions[i])) {
                    requestPermissions(permissions, 321);//调用方法获取权限
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        onRequestPermissionsResult(requestCode, grantResults);
//        PostCardActivityPer
    }

    public void finish(View view) {
        finish();
    }


}
