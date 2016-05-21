package com.cdd.detection.matching;

import android.util.Log;
import com.cdd.detection.utils.FileEnvironment;
import com.cdd.detection.utils.MatchUtils;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.cpp.opencv_core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_core.cvMatMul;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

/**
 * Created by channerduan on 5/20/16.
 */
public class DataEngine {

    static final class Holder {
        static final DataEngine mInstance = new DataEngine();
    }

    public static DataEngine getInstance() {
        return Holder.mInstance;
    }

    public static final float VERIFYING_THRESHOLD = 4000f;

    public static final int FORM_WIDTH = 47;
    public static final int FORM_HEIGHT = 78;

    public static final float FORM_W_H_RATIO = (float) FORM_WIDTH / FORM_HEIGHT;

    private opencv_core.CvMat mEarProjectBase;
    private List<SubjectBean> mData = new ArrayList<>();

    public void init() {
        loadBase();
        update();
    }

    public List<SubjectBean> getCoreData() {
        return mData;
    }

    public void deleteSubjectByName(String name) {
        for (SubjectBean bean : mData) {
            if (bean.name.equals(name)) {
                FileEnvironment.delete(new File(FileEnvironment.getMatchingImagePath()
                        + "/" + name));
            }
        }
        update();
    }

    public int getSubjectNum() {
        return mData.size();
    }

    public boolean nameExist(String name) {
        for (SubjectBean bean : mData) {
            if (bean.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String matching(IplImage image) {
        CvMat sampleMat = imgToProjectBase(image);
        int i, j;
        float minDist, tmpMinDist, tmpf;
        SubjectBean target = null;
        SubjectBean subject;
        minDist = 99999999;
        for (i = 0;i < mData.size();i++) {
            subject = mData.get(i);
            tmpMinDist = 99999999;
            for (j = 0;j < subject.listProjectOnBase.size();j++) {
                tmpf = MatchUtils.calcuDistance(sampleMat, subject.listProjectOnBase.get(j));
                if (tmpMinDist > tmpf) {
                    tmpMinDist = tmpf;
                }
            }
            Log.e("channer test", "channer test matching ->" + subject.name + "<- min:" + tmpMinDist);
            if (minDist > tmpMinDist) {
                minDist = tmpMinDist;
                target = subject;
            }
        }
        if (minDist < VERIFYING_THRESHOLD) {
            return target.name;
        } else {
            return "";
        }
    }

    public void createSubject(String subjectName) {
        FileEnvironment.moveDirectory(FileEnvironment.getTmpImagePath() + subjectName,
                FileEnvironment.getMatchingImagePath() + subjectName);
        update();
    }

    private void loadBase() {
        String filepath = FileEnvironment.baseAppOutputPath + "/ear_base_2d.xml";
        opencv_core.CvFileStorage fileStorage;
        fileStorage = cvOpenFileStorage(filepath, null, CV_STORAGE_READ, null);
        Pointer pointer = cvReadByName(
                fileStorage,
                null,
                "ear_base_2d");
        mEarProjectBase = new opencv_core.CvMat(pointer);
        cvReleaseFileStorage(fileStorage);
//        Log.e("channer test", "channer test mEarProjectBase: " + mEarProjectBase.toString());
    }

    private void update() {
        mData.clear();
        String filepath = FileEnvironment.getMatchingImagePath();
        Log.e("channer test", "channer test " + filepath);
        File file = new File(filepath);
        File[] childFiles = file.listFiles();
        for (int i = 0; i < childFiles.length; i++) {
            if (childFiles[i].isDirectory()) {
                Log.e("channer test", "channer test, load subject: " + childFiles[i].getName());
                mData.add(new SubjectBean(childFiles[i].getName(), loadOneImageSet(childFiles[i])));
            }
        }
    }

    private CvMat tmpMat = cvCreateMat(
            FORM_HEIGHT, // rows
            FORM_WIDTH, // cols
            CV_32FC1);
    // the image have to be standard size
    private CvMat imgToProjectBase(opencv_core.IplImage img) {
        cvConvert(img.asCvMat(), tmpMat);
        CvMat baseMat = cvCreateMat(
                FORM_HEIGHT, // rows
                mEarProjectBase.cols(), // cols
                CV_32FC1);
        cvMatMul(tmpMat, mEarProjectBase, baseMat);
        return baseMat;
    }

    private List<CvMat> loadOneImageSet(File file) {
        List<CvMat> listProjectedMat = new ArrayList<>();
        File[] childFiles = file.listFiles();
        opencv_core.IplImage iplImage;
        for (int i = 0; i < childFiles.length; i++) {
            if (childFiles[i].isFile()) {
                iplImage = cvLoadImage(
                        childFiles[i].getAbsolutePath(), // filename
                        CV_LOAD_IMAGE_GRAYSCALE);
                listProjectedMat.add(imgToProjectBase(iplImage));
            }
        }
        return listProjectedMat;
    }

//                String filepath = FileEnvironment.baseAppOutputPath + "/test.xml";
//                opencv_core.CvFileStorage fileStorage;
//                fileStorage = cvOpenFileStorage(filepath, null, CV_STORAGE_WRITE, null);
//                CvMat testMat = cvCreateMat(
//                        47, // rows
//                        6, // cols
//                        CV_32FC1);
//                cvWrite(
//                        fileStorage, // fs
//                        "test_matrix", // name
//                        testMat);
//                cvReleaseFileStorage(fileStorage);
}
