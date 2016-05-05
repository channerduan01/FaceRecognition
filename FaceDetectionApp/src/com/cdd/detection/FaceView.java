package com.cdd.detection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import com.cdd.detection.utils.FileEnvironment;
import com.cdd.detection.utils.ImgUtils;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_objdetect;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

/**
 * Created by channerduan on 5/5/16.
 */
class FaceView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 2;

    private opencv_core.IplImage grayImage;
    private opencv_objdetect.CvHaarClassifierCascade classifier;
    private opencv_core.CvMemStorage storage;
    private opencv_core.CvSeq faces;

    public FaceView(Context context) throws IOException {
        super(context);

        String path = FileEnvironment.baseAppOutputPath + "/haarcascade_frontalface_alt.xml";
        Log.e("channer test", path);
        // Load the classifier file from Java resources.
//        File classifierFile = Loader.extractResource(getClass(),
//                path,
//            context.getCacheDir(), "classifier", ".xml");

        File classifierFile = new File(path);

        if (classifierFile == null || classifierFile.length() <= 0) {
            throw new IOException("Could not extract the classifier file from Java resource.");
        }

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);
        classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
        if (classifier.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        storage = opencv_core.CvMemStorage.create();
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        }
    }

    private int mNumber = 0;
    protected void processImage(byte[] data, int width, int height) {
        // First, downsample our image and convert it into a grayscale IplImage
        int f = SUBSAMPLING_FACTOR;
        if (grayImage == null || grayImage.width() != width / f || grayImage.height() != height / f) {
            grayImage = opencv_core.IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);
        }
        int imageWidth = grayImage.width();
        int imageHeight = grayImage.height();
        int dataStride = f * width;
        int imageStride = grayImage.widthStep();
        ByteBuffer imageBuffer = grayImage.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y * dataStride;
            int imageLine = y * imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f * x]);
            }
        }
        opencv_core.IplImage tmpImage = ImgUtils.rotate(grayImage, 270);
        cvClearMemStorage(storage);
        faces = cvHaarDetectObjects(tmpImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
        mNumber++;
        int res = cvSaveImage(FileEnvironment.getTmpImagePath() + "/test" + mNumber + ".jpg", tmpImage);
        Log.e("channer", "save image res:" + res);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(20);

        String s = "FacePreview - This side up.";
        float textWidth = paint.measureText(s);
        canvas.drawText(s, (getWidth() - textWidth) / 2, 20, paint);

        if (faces != null) {
            Log.e("channer test", "find faces:" + faces.total());
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
//            float scaleX = (float) getWidth() / grayImage.width();
//            float scaleY = (float) getHeight() / grayImage.height();

            float scaleX = (float) getWidth() / grayImage.height() ;
            float scaleY = (float) getHeight() / grayImage.width();

            int total = faces.total();
            for (int i = 0; i < total; i++) {
                opencv_core.CvRect r = new opencv_core.CvRect(cvGetSeqElem(faces, i));
                int x = r.x(), y = r.y(), w = r.width(), h = r.height();
                canvas.drawRect(x * scaleX, y * scaleY, (x + w) * scaleX, (y + h) * scaleY, paint);

            }
        }
    }
}