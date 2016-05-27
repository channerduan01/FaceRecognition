package com.cdd.detection.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.cdd.detection.EarApplication;

import java.io.*;

/**
 * Created by channerduan on 5/5/16.
 */
public class FileEnvironment {

    public static String baseAppOutputPath;
    public static String tmpImagePath;
    public static String matchingImagePath;

    public static void initAppDirectory(Context ctx) throws Exception {
        File file = ctx.getExternalCacheDir();
        if (file == null) {
            throw new Exception("sdcard storage error!!!");
        }
        baseAppOutputPath = file.getAbsolutePath() + "/";
        Log.e("channer test", "init basic filepath: " + baseAppOutputPath);
        tmpImagePath = baseAppOutputPath + "images/";
        matchingImagePath = baseAppOutputPath + "matching/";
        file = new File(tmpImagePath);
        if (!file.exists())
            file.mkdir();
        file = new File(matchingImagePath);
        if (!file.exists())
            file.mkdir();

        coreDataCheck();
    }

    public static final String HAAR_L_EAR_FILENAME = "haarcascade_mcs_leftear.xml";
    public static final String HAAR_R_EAR_FILENAME = "haarcascade_mcs_rightear.xml";
    public static final String PCA_2D_BASE_SETTING_FILENAME = "ear_base_2d.xml";
//    public static final String PCA_2D_BASE_SETTING_FILENAME = "ear_base_2d_extra.xml";


    private static void coreDataCheck() {
        if (new File(baseAppOutputPath + HAAR_L_EAR_FILENAME).exists() &&
                new File(baseAppOutputPath + HAAR_R_EAR_FILENAME).exists() &&
                new File(baseAppOutputPath + PCA_2D_BASE_SETTING_FILENAME).exists()) {
            Log.e("channer test", "channer test basic data complete.");
            return;
        }
        Log.e("channer test", "channer test start init basic data from assets!");
        extractFromAssetsFile(HAAR_L_EAR_FILENAME);
        extractFromAssetsFile(HAAR_R_EAR_FILENAME);
        extractFromAssetsFile(PCA_2D_BASE_SETTING_FILENAME);
    }

    public static void extractFromAssetsFile(String fileName) {
        AssetManager am = EarApplication.getInstance().getResources().getAssets();
        try{
            FileOutputStream out = new FileOutputStream(baseAppOutputPath + fileName);
            InputStream in = am.open(fileName);
            copyForStream(in, out);
            in.close();
            out.close();
        }catch(Exception e){
        }
    }

    private static void copyForStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
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
