package com.cdd.detection.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Created by duanchao01 on 2014/8/4.
 */
public class FileEnvironment {

    public static String baseAppOutputPath;
    public static String tmpImagePath;
    public static String matchingImagePath;


    /**
     * 初始化
     */
    public static void initAppDirectory(Context ctx) {
        File file = ctx.getExternalCacheDir();
        if (file == null) {
            return;
        }
        baseAppOutputPath = file.getAbsolutePath();
        tmpImagePath = baseAppOutputPath + "/images/";
        matchingImagePath = baseAppOutputPath + "/matching/";
        file = new File(tmpImagePath);
        if (!file.exists())
            file.mkdir();
        file = new File(matchingImagePath);
        if (!file.exists())
            file.mkdir();
        Log.e("channer test", "init basic filepath: " + baseAppOutputPath);
    }

    public static String getTmpImagePath() {
        return tmpImagePath;
    }

    public static String getMatchingImagePath() {
        return matchingImagePath;
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

    public static boolean moveDirectory(String srcDirName, String destDirName) {
        File srcDir = new File(srcDirName);
        File destDir = new File(destDirName);
        if(!destDir.exists())
            destDir.mkdirs();
        File[] sourceFiles = srcDir.listFiles();
        for (File sourceFile : sourceFiles) {
            if (sourceFile.isFile())
                moveFile(sourceFile.getAbsolutePath(), destDir.getAbsolutePath());
            else if (sourceFile.isDirectory())
                moveDirectory(sourceFile.getAbsolutePath(),
                        destDir.getAbsolutePath() + File.separator + sourceFile.getName());
        }
        return srcDir.delete();
    }

    public static boolean moveFile(String srcFileName, String destDirName) {
        File srcFile = new File(srcFileName);
        File destDir = new File(destDirName);
        if (!destDir.exists())
            destDir.mkdirs();
        return srcFile.renameTo(new File(destDirName + File.separator + srcFile.getName()));
    }

}
