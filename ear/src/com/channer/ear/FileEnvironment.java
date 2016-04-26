package com.channer.ear;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by duanchao01 on 2014/8/4.
 */
public class FileEnvironment {

    private static String baseAppOutputPath;

    /**
     * 初始化
     */
    public static void initAppDirectory(Context ctx) {
        File file = ctx.getExternalCacheDir();
        if(file == null) {
            return;
        }
        baseAppOutputPath = file.toString();
        Log.e("channer test", "got basic filepath: " + baseAppOutputPath);
    }

    public static String getTmpImagePath() {
        return baseAppOutputPath + "/";
    }

}
