package com.cdd.detection;

import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.cdd.detection.imagebasis.Rect;
import com.cdd.detection.utils.CameraUtils;
import com.cdd.detection.utils.FileEnvironment;
import com.cdd.detection.utils.ImgUtils;
import com.cdd.detection.views.CameraPreviewView;
import com.cdd.detection.views.PhotoFrameView;
import com.googlecode.javacv.cpp.opencv_core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

public class MyActivity extends Activity implements DetectEngine.CapturedCallback {
    private PhotoFrameView photoFrameView;
    private CameraPreviewView cameraPreviewView;

    private DetectEngine detectEngine;


    private TextView fpsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            initCamera();
            initUI();
        } catch (IOException e) {
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
    }

    private void initUI() {
        fpsTextView = (TextView) findViewById(R.id.tv_fps_txt);
    }

    private void initCamera() throws IOException {
        // obtain the basic settings and sizes
        Camera camera = CameraUtils.getCameraInstance();
        int image_width = camera.getParameters().getPreviewSize().width;
        int image_height = camera.getParameters().getPreviewSize().height;
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        Log.e("channer test", "channer test DisplayMetrics size: " + metrics.widthPixels + ", " + metrics.heightPixels);
        int view_width = metrics.widthPixels;
        int view_height = (int) (metrics.widthPixels * (float) image_width / (float) image_height + 0.5f);
        Log.e("channer test", "channer test View size: " + view_width + ", " + view_height);

        // setup engine and views
        detectEngine = new DetectEngine();
        detectEngine.setCallback(this);
        FrameLayout container = (FrameLayout) findViewById(R.id.camera_preview);
        Log.e("channer test", "channer test Camera size: " + image_width + ", " + image_height);
        photoFrameView = new PhotoFrameView(this);
        cameraPreviewView = new CameraPreviewView(this, camera, detectEngine);

        // launch
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(view_width, view_height);
        layoutParams.setMargins(0, (int) ((metrics.heightPixels - view_height) / 2f + 0.5f), 0, 0);
        container.addView(cameraPreviewView, layoutParams);
        container.addView(photoFrameView, layoutParams);
    }

    float frameNumber = 0;
    long startTimestampe = System.currentTimeMillis();


    @Override
    public void findTarget(List<Rect> rectList, opencv_core.IplImage data, int width, int height) {
        photoFrameView.update(rectList, width, height);
        frameNumber++;

        float fps = frameNumber / (System.currentTimeMillis() - startTimestampe) * 1000f;
        fpsTextView.setText(String.format("%.1f", fps) + " fps");
        if (!rectList.isEmpty()) {
//            opencv_core.IplImage image = opencv_core.IplImage.create(height, width, IPL_DEPTH_8U, 1);
//            ByteBuffer imageBuffer = image.getByteBuffer();
//            imageBuffer.put(data, 0, width*height);
            cvSaveImage(FileEnvironment.getTmpImagePath() + "/test" + (int)frameNumber + ".jpg",
                    ImgUtils.crop(data, rectList.get(0)));
        }
    }
}
