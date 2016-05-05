package com.cdd.detection.utils;

import com.googlecode.javacv.cpp.opencv_core;

import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import static com.googlecode.javacv.cpp.opencv_core.cvTranspose;

/**
 * Created by channerduan on 5/5/16.
 */
public class ImgUtils {
    public static opencv_core.IplImage rotate(opencv_core.IplImage IplSrc, int angle) {
        opencv_core.IplImage img = opencv_core.IplImage.create(IplSrc.height(), IplSrc.width(), IplSrc.depth(), IplSrc.nChannels());
        cvTranspose(IplSrc, img);
        cvFlip(img, img, angle);
        return img;
    }
}
