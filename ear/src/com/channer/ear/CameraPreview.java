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

import java.io.*;


/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    public interface CapturedCallback {
        void callback();
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

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    private int[] pixels_int;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        int w = camera.getParameters().getPreviewSize().width;
        int h = camera.getParameters().getPreviewSize().height;
        Log.e("channer test", "channer test got preview frame: " + w + ", " + h);
//        decodeYUV420SP_GrayScale(data, w, h);
        decodeYUV420SP(pixels_int, data, w, h);
        save(w, h);
        if (mCallback != null)
            mCallback.callback();
    }

    private int mNumber = 0;

    private void save(int w, int h) {
        mNumber++;
        Bitmap bmp = Bitmap.createBitmap(pixels_int, w, h, Bitmap.Config.ARGB_8888);
        // Rotating Bitmap
        Matrix mtx = new Matrix();
        mtx.postRotate(90);
        bmp = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        String path = FileEnvironment.getTmpImagePath() + mCaptureName + "/" + mNumber + ".jpg";
        Log.e("channer test", "channer test saved: " + path);
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
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            ///initialize the variables
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            pixels_int = new int[previewSize.width * previewSize.height];
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
//            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e("channer test", "channer test Error setting camera preview: " + e.getMessage());
        }
    }
}