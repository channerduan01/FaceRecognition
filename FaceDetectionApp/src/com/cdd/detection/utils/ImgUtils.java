package com.cdd.detection.utils;

import com.cdd.detection.imagebasis.Rect;
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
