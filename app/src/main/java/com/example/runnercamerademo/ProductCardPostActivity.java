package com.example.runnercamerademo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;


public class ProductCardPostActivity extends PostCardActivity {

    private static final int REQUEST_PERMISSION = 1;
    private  String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_product_card_post;
    }

    @Override
    protected View getLayoutView() {
        return null;
    }

    @Override
    protected void initView() {
    }

    // 启动相机Fragment
    private void setFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, CameraFragment.newInstance())
                .commitNowAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}