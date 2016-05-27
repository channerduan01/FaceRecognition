package com.cdd.soton.utils;

import com.googlecode.javacv.cpp.opencv_core;

import java.nio.FloatBuffer;


/**
 * Created by channerduan on 5/21/16.
 */
public class MatchUtils {

    public static float calcuDistance(opencv_core.CvMat mat1, opencv_core.CvMat mat2) {
        float distance = 0f;
        FloatBuffer floatBuf1 = mat1.getFloatBuffer();
        FloatBuffer floatBuf2 = mat2.getFloatBuffer();
        int row_num = mat1.rows();
        int col_num = mat1.cols();
        int i, j, idx;
        float tmpF, sumF;
        for (i = 0; i < col_num; i++) {
            sumF = 0;
            for (j = 0; j < row_num; j++) {
                idx = j * col_num + i;
                tmpF = floatBuf1.get(idx) - floatBuf2.get(idx);
                sumF += tmpF * tmpF;
            }
            distance += Math.sqrt(sumF);
        }
        return distance;
//        Log.e("channer test", "channer test calcuDistance mat1: "
//                + mat1.toString());
//        Log.e("channer test", "channer test calcuDistance mat2: "
//                + mat2.toString());

//        if (1 == 1) return 0f;
//
//
//        opencv_core.CvMat cvSub = cvCreateMat(
//                mat1.rows(), // rows
//                mat1.cols(), // cols
//                CV_32FC1);
//
//        float distance = 0f;
//        opencv_core.CvMat mat1_trans = cvCreateMat(
//                mat1.cols(), // rows
//                mat1.rows(), // cols
//                CV_32FC1);
//        cvTranspose(mat1, mat1_trans);
//
//        opencv_core.CvMat resMat = cvCreateMat(
//                1, // rows
//                1, // cols
//                CV_32FC1);
//        opencv_core.CvMat cvMat1 = cvCreateMat(
//                1, // rows
//                mat1_trans.cols(), // cols
//                CV_32FC1);
//        opencv_core.CvMat cvMat2 = cvCreateMat(
//                mat2.rows(), // rows
//                1, // cols
//                CV_32FC1);
//        for (int i = 0; i < mat1_trans.rows(); i++) {
//            cvGetRow(mat1_trans, cvMat1, i);
//            cvGetCol(mat2, cvMat2, i);
////            Log.e("channer test", "channer test (" + cvMat1.rows() + ", " + cvMat1.cols() + ") ("
////                    + cvMat2.rows() + ", " + cvMat2.cols() + ") " + cvMat2.get().length + " " + cvMat2.toString());
//            cvMatMul(cvMat1, cvMat2, resMat);
////            Log.e("channer test", "channer test resMat size: " + resMat.get().length + " " + resMat.toString());
//            distance += resMat.get()[0];
//        }
//        return distance;
    }
}
