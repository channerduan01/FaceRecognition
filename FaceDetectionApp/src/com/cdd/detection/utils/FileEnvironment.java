package com.cdd.detection.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Created by duanchao01 on 2014/8/4.
 */
public class FileEnvironment {

    public static String baseAppOutputPath;

    /**
     * 初始化
     */
    public static void initAppDirectory(Context ctx) {
        File file = ctx.getExternalCacheDir();
        if (file == null) {
            return;
        }
        baseAppOutputPath = file.toString();
        file = new File(baseAppOutputPath + "/images/");
        if (!file.exists()) {
            file.mkdir();
        }
        Log.e("channer test", "got basic filepath: " + baseAppOutputPath);
    }

    public static String getTmpImagePath() {
        return baseAppOutputPath + "/images/";
    }

    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

}
