package com.cdd.soton.detect;

import android.hardware.Camera;
import com.cdd.soton.recognize.MatchEngine;
import com.cdd.soton.utils.FileEnvironment;
import com.cdd.soton.utils.ImgUtils;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_objdetect;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

/**
 * Created by channerduan on 5/5/16.
 */
public class DetectEngine implements Camera.PreviewCallback {

    private static final float MINI_RATIO = 4.0f;

    public interface CapturedCallback {
        void findTarget(List<Rect> rectList, IplImage data, int width, int height);
    }

    private CapturedCallback mCallback = null;

    public void setCallback(CapturedCallback callback) {
        mCallback = callback;
    }

    private opencv_core.IplImage grayImage;
    private List<opencv_objdetect.CvHaarClassifierCascade> classifierList;
    private opencv_core.CvMemStorage storage;
    private opencv_core.CvSeq detectRes;


    private static String[] CLASSIFIER_FILE_PATHS = {
            FileEnvironment.baseAppOutputPath + FileEnvironment.HAAR_L_EAR_FILENAME,
            FileEnvironment.baseAppOutputPath + FileEnvironment.HAAR_R_EAR_FILENAME,
    };

    public DetectEngine() throws IOException {
        classifierList = new ArrayList<>();
        for (String filepath : CLASSIFIER_FILE_PATHS)
            classifierList.add(loadClassifier(filepath));
        storage = opencv_core.CvMemStorage.create();
    }

    private opencv_objdetect.CvHaarClassifierCascade loadClassifier(String filepath) throws IOException {
        File classifierFile = new File(filepath);
        if (classifierFile == null || classifierFile.length() <= 0) {
            throw new IOException("Could not extract the classifier file from Java resource.");
        }
        Loader.load(opencv_objdetect.class);
        opencv_objdetect.CvHaarClassifierCascade classifier =
                new opencv_objdetect.CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
        if (classifier.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        return classifier;
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
        }
    }

    private void processImage(byte[] data, int width, int height) {
        // First, downsample our image and convert it into a grayscale IplImage
//        int f = SUBSAMPLING_FACTOR;
//        if (grayImage == null || grayImage.width() != width / f || grayImage.height() != height / f) {
//            grayImage = opencv_core.IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);
//        }
        if (grayImage == null) {
            grayImage = opencv_core.IplImage.create(width, height, IPL_DEPTH_8U, 1);
        }
//        Log.e("channer test", "channer test " + width + " " + height);
        ByteBuffer imageBuffer = grayImage.getByteBuffer();
        imageBuffer.put(data, 0, height * width);

        opencv_core.IplImage tmpImage = ImgUtils.rotate(grayImage, 270);

//        mNumber++;
//        int res = cvSaveImage(FileEnvironment.getTmpImagePath() + "/test" + mNumber + ".jpg", tmpImage);
//        if (res != 0)
//            Log.e("channer test", "channer test saved image:" + mNumber);

        List<Rect> rectList = new ArrayList<>();
        opencv_objdetect.CvHaarClassifierCascade classifier;
        for (int k = 0; k < classifierList.size(); k++) {
            classifier = classifierList.get(k);
            cvClearMemStorage(storage);
//            detectRes = cvHaarDetectObjects(tmpImage,
//                    classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
            detectRes = cvHaarDetectObjects(tmpImage,
                    classifier, storage, 1.1, 1, CV_HAAR_FIND_BIGGEST_OBJECT,
                    generateMiniCvSize(width),  // consider rotation, so used width here!
                    CvSize.ZERO);
            if (detectRes.total() > 0) {
                for (int i = 0; i < detectRes.total(); i++) {
                    CvRect r = new CvRect(cvGetSeqElem(detectRes, i));
//                    int x = r.x(), y = r.y(), w = r.width(), h = r.height();
//                    Log.e("channer test", "channer test find face: (" + x + ", " + y +
//                            ") -> w:" + w + " height:" + h);
//                    rectList.add(new Rect(
//                            r.x() * SUBSAMPLING_FACTOR,
//                            r.y() * SUBSAMPLING_FACTOR,
//                            r.width() * SUBSAMPLING_FACTOR,
//                            r.height() * SUBSAMPLING_FACTOR));
                    // tricky
                    // actually, left-classifier detects right ear, so k == 0 here!
                    rectList.add(new Rect(r.x(), r.y(), r.width(), r.height(), k == 0));
                }
            }
        }
        Collections.sort(rectList);
        Collections.reverse(rectList);
        if (mCallback != null)
            mCallback.findTarget(rectList, tmpImage, height, width);
    }

    private CvSize generateMiniCvSize(float hightCaptured) {
        float miniHeight = hightCaptured / MINI_RATIO;
        return new CvSize((int) (miniHeight * MatchEngine.FORM_W_H_RATIO + 0.5f),
                (int) (miniHeight + 0.5f));
    }
}