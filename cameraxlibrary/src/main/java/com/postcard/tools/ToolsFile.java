package com.postcard.tools;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;

import java.io.File;
import java.util.UUID;

/**
 * 全局共用的对文件操作的方法
 *
 * @author wangyue
 */
public class ToolsFile {
    /**
     * 获取图片目录
     *
     * @return 图片目录（/storage/emulated/0/Pictures）
     */
    public static File getExtPicturesPath() {
        File extPicturesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!extPicturesPath.exists()) {
            extPicturesPath.mkdir();
        }
        return extPicturesPath;
    }

    public static String createImagePathUri(Context activity) {
        String potoName = String.valueOf(UUID.randomUUID());
        File takePictureFile = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //适配 Android Q
            File base = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String pathName = new StringBuffer().append(base.getPath()).append(File.separator)
                    .append(potoName).append(".jpg").toString();
            takePictureFile = new File(pathName);
        } else {
            String pathName = new StringBuffer().append(getExtPicturesPath()).append(File.separator)
                    .append(potoName).append(".jpg").toString();
            takePictureFile = new File(pathName);

        }
        return takePictureFile.getAbsolutePath();
    }

}
