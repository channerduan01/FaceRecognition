package com.cdd.soton.utils;

import com.cdd.soton.detect.Rect;
import com.googlecode.javacv.cpp.opencv_core;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;

/**
 * Created by channerduan on 5/5/16.
 */
public class ImgUtils {
    public static opencv_core.IplImage rotate(opencv_core.IplImage iplSrc, int angle) {
        opencv_core.IplImage img = opencv_core.IplImage.create(iplSrc.height(), iplSrc.width(), iplSrc.depth(), iplSrc.nChannels());
        cvTranspose(iplSrc, img);
        cvFlip(img, img, angle);
        return img;
    }

    public static opencv_core.IplImage mirror(opencv_core.IplImage iplSrc) {
        int col_num = iplSrc.width();
        int row_num = iplSrc.height();
        opencv_core.IplImage des = opencv_core.IplImage.create(col_num, row_num, iplSrc.depth(), iplSrc.nChannels());
        int i, j;
        for (i = 0; i < row_num; i++) {
            for (j = 0; j < col_num; j++) {
                cvSet2D(des, i, col_num - j - 1, cvGet2D(iplSrc, i, j));
            }
        }

        return des;
    }

    public static opencv_core.IplImage crop(opencv_core.IplImage iplSrc, Rect cropRect) {
        cvSetImageROI(iplSrc, cropRect.trans2CvRect());
        opencv_core.IplImage cropped = cvCreateImage(cvGetSize(iplSrc), iplSrc.depth(), iplSrc.nChannels());
        cvCopy(iplSrc, cropped);
        return cropped;
    }

    public static opencv_core.IplImage resize(opencv_core.IplImage iplSrc, CvSize cvSize) {
        opencv_core.IplImage resizedImage =
                opencv_core.IplImage.create(cvSize.width(), cvSize.height(), iplSrc.depth(),
                        iplSrc.nChannels());
        cvResize(iplSrc, resizedImage);
        return resizedImage;
    }
}
