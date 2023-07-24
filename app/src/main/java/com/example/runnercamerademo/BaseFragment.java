package com.example.runnercamerademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * Fragment基类
 */
public abstract class BaseFragment extends Fragment {
    /**
     * 主界面Activity
     */
    public Activity mActivity = null;
    /**
     * fragment要展示的view
     */
    public View baseView = null;
    protected ActivityResultLauncher<Intent> someActivityResultLauncher;

    public void startActivityForResult(Intent intent) {
        someActivityResultLauncher.launch(intent);
    }

    protected void onActivityResult(Intent data) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (baseView == null) {
            int layoutId = getLayoutId();
            if (layoutId != 0) {
                mActivity = getActivity();
                baseView = inflater.inflate(layoutId, container, false);
            } else {
                throw new IllegalStateException("layoutId can not be 0");
            }
        }
        initView();
        return baseView;
    }

    /**
     * 设置布局文件ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化view视图
     */
    protected abstract void initView();

    protected View findViewById(int id) {
        return baseView.findViewById(id);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request code
                            Intent data = result.getData();
                            BaseFragment.this.onActivityResult(data);
                        }
                    }
                });
    }


    private boolean isFrist;

    @Override
    public void onResume() {
        super.onResume();
        Log.e("onResume", "onResume");
        if (!isFrist) {
            onFristShow();
            isFrist = true;
        }
    }


    public void onFristShow() {

    }
}
