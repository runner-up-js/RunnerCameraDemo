package com.example.runnercamerademo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;
import com.postcard.camerax.CameraPreview;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CameraFragment extends BaseFragment implements CameraPreview.OnPictureListener {
    private CameraPreview cameraView;
    private TabLayout tabsVIew;
    private String[] tabs = new String[]{"竖版", "横版"};

    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_camera;
    }

    @Override
    protected void initView() {
        tabsVIew = (TabLayout) findViewById(R.id.tabs);
        cameraView = (CameraPreview) findViewById(R.id.camer_preview);
        cameraView.setOnPictureListener(this);
        tabsVIew.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tabsVIew.getSelectedTabPosition();
                if (pos == 1) {
                    cameraView.setVideoOrientation(CameraPreview.OR_landscape);
                } else {
                    cameraView.setVideoOrientation(CameraPreview.OR_portrait);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        findViewById(R.id.take_poto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraFragmentPermissionsDispatcher.takePicWithPermissionCheck(CameraFragment.this);
            }
        });
//        findViewById(R.id.check_poto).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CameraFragmentPermissionsDispatcher.selectPictureWithPermissionCheck(CameraFragment.this);
//            }
//        });
//        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().finish();
//            }
//        });
        tabsVIew.addTab(tabsVIew.newTab().setText(tabs[0]));
        tabsVIew.addTab(tabsVIew.newTab().setText(tabs[1]));
        cameraView.setVideoOrientation(CameraPreview.OR_landscape);
        CameraFragmentPermissionsDispatcher.initCameraWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void initCamera() {
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void takePic() {
        cameraView.takePicture();
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void selectPicture() {

    }



    @Override
    public void onImageSaved(Uri savedUri) {
        Toast.makeText(getActivity(),"拍照成功："+savedUri.getPath(),Toast.LENGTH_LONG).show();
//        openPreview(savedUri.getPath());
    }
}