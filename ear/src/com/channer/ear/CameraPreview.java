package com.channer.ear;

/**
 * Created by channerduan on 4/20/16.
 */

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_objdetect;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    public static interface CapturedCallback {
        void callback();

        void findTarget(List<Rect> rectList);
    }

    private CapturedCallback mCallback = null;

    public void setCallback(CapturedCallback callback) {
        mCallback = callback;
    }

    private String mCaptureName = null;

    public void capture(String captureName) {
        mCaptureName = captureName;
        if (TextUtils.isEmpty(mCaptureName)) {
            mCamera.setPreviewCallback(null);
        } else {
            mCamera.setPreviewCallback(this);
            mNumber = 0;
        }
    }

    private SurfaceHolder mHolder;
    private Camera mCamera;

    private IplImage grayImage;
    private IplImage grayImage_tmp;
    private opencv_objdetect.CvHaarClassifierCascade classifier;
    private CvMemStorage storage;
    private CvSeq faces;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        initClassifier();


        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //test
        mCaptureName = "test";
        mCamera.setPreviewCallback(this);
    }

    private byte[] pixels;
    private int[] pixels_int;

    private void initClassifier() {
        String path = FileEnvironment.baseAppOutputPath + "/haarcascade_frontalface_alt.xml";
        File classifierFile = new File(path);
        Loader.load(opencv_objdetect.class);
        classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
        storage = CvMemStorage.create();
    }


    private static IplImage rotate(IplImage IplSrc, int angle) {
        IplImage img = IplImage.create(IplSrc.height(), IplSrc.width(), IplSrc.depth(), IplSrc.nChannels());
        cvTranspose(IplSrc, img);
        cvFlip(img, img, angle);
        return img;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        int w = camera.getParameters().getPreviewSize().width;
        int h = camera.getParameters().getPreviewSize().height;
        Log.e("channer test", "channer test got preview frame: " + w + ", " + h);
//        decodeYUV420SP_GrayScale(data, w, h);

        if (grayImage_tmp == null) {
//            grayImage = IplImage.create(w, h, IPL_DEPTH_8U, 1);
            grayImage_tmp = IplImage.create(w, h, IPL_DEPTH_8U, 1);
        }

        ByteBuffer imageBuffer = grayImage_tmp.getByteBuffer();
        int size = w * h;
        for (int i = 0; i < size; i++) imageBuffer.put(i, data[i]);
        grayImage = rotate(grayImage_tmp, -90);
        mNumber++;
        cvSaveImage(FileEnvironment.getTmpImagePath() + mCaptureName + "/" + mNumber + ".jpg", grayImage);

        Log.e("channer test", "channer test got grayImage frame: " + grayImage.width() + ", " + grayImage.height());
//        imageBuffer.put(data);
//        Log.e("channer test", "got cvsize: " + grayImage.cvSize().width() + ", " + grayImage.cvSize().height());
        cvClearMemStorage(storage);
        faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 2, CV_HAAR_DO_CANNY_PRUNING);


        Log.e("channer test", "channer test find faces:" + faces.total());
        if (faces.total() != 0) {
            List<Rect> rectList = new ArrayList<>();
            for (int i = 0; i < faces.total(); i++) {
                CvRect r = new CvRect(cvGetSeqElem(faces, i));
                int x = r.x(), y = r.y(), width = r.width(), height = r.height();
                Log.e("channer test", "channer test find face: (" + x + ", " + y +
                        ") -> w:" + width + " height:" + height);
                rectList.add(new Rect(x, y, w, h));
//                canvas.drawRect(x*scaleX, y*scaleY, (x+w)*scaleX, (y+h)*scaleY, paint);
            }
            if (mCallback != null)
                mCallback.findTarget(rectList);
        } else {
            if (mCallback != null)
                mCallback.findTarget(null);
        }

//        Log.e("Pixels", "The top right pixel has the following RGB (hexadecimal) values:"
//                +Integer.toHexString(pixels[0]));

//        save(w, h);
        if (mCallback != null)
            mCallback.callback();
    }

    private int mNumber = 0;

    private void save(int w, int h) {
        mNumber++;
//        StringBuffer buffer = new StringBuffer();
//        for (int i = 0;i < pixels_int.length;i++) {
////            pixels_int[i] = pixels[i];
//        }
//        System.out.print(buffer);

        Log.e("channer test", "channer test " + pixels_int[1000]);

        Bitmap bmp = Bitmap.createBitmap(pixels_int, w, h, Bitmap.Config.ARGB_8888);
        // Rotating Bitmap
        Matrix mtx = new Matrix();
        mtx.postRotate(-90);
        bmp = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        String path = FileEnvironment.getTmpImagePath() + mCaptureName + "/" + mNumber + ".jpg";
        Log.e("test", path);
        File file = new File(FileEnvironment.getTmpImagePath() + mCaptureName + "/" + mNumber + ".jpg");
        OutputStream outStream;
        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts YUV420 NV21 to Y888 (RGB8888). The grayscale image still holds 3 bytes on the pixel.
     *
     * @param yuv420sp byte array on YUV420 NV21 format.
     * @param width    pixels width
     * @param height   pixels height
     */
    public void decodeYUV420SP_GrayScale(byte[] yuv420sp, int width, int height) {
        int p;
        int size = width * height;
        for (int i = 0; i < size; i++) {
            p = yuv420sp[i] & 0xFF;
            pixels_int[i] = 0xff000000 | p << 16 | p << 8 | p;
//            pixels[i] = (byte) (0xff000000 | p<<16 | p<<8 | p);
        }
    }

    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        mCamera.setParameters(parameters);
//        if (previewCallback != null) {
//            mCamera.setPreviewCallbackWithBuffer(previewCallback);
//            Camera.Size size = parameters.getPreviewSize();
//            byte[] data = new byte[size.width * size.height *
//                    ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
//            mCamera.addCallbackBuffer(data);
//        }
//        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            ///initialize the variables
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            pixels = new byte[previewSize.width * previewSize.height];
            pixels_int = new int[pixels.length];
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e("channer test", "channer test Error setting camera preview: " + e.getMessage());
        }
    }
}