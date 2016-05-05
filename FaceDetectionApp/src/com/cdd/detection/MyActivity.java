package com.cdd.detection;

import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.cdd.detection.utils.CameraUtils;

import java.io.IOException;

public class MyActivity extends Activity {
    private FrameLayout container;
    private FaceView faceView;
    private CameraPreviewView cameraPreviewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create our Preview view and set it as the content of our activity.
        try {
            initCamera();
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
    }

    private void initCamera() throws IOException {
        Camera camera = CameraUtils.getCameraInstance();
        int c_width = camera.getParameters().getPreviewSize().width;
        int c_height = camera.getParameters().getPreviewSize().height;
        container = new FrameLayout(this);
        Log.e("channer test", "Camera size: " + c_width + ", " + c_height);
        faceView = new FaceView(this);
        cameraPreviewView = new CameraPreviewView(this, camera, faceView);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        Log.e("channer test", "DisplayMetrics size: " + metrics.widthPixels + ", " + metrics.heightPixels);
        int v_width = metrics.widthPixels;
        int v_height = (int) (metrics.widthPixels * (float) c_width / (float) c_height + 0.5f);
        Log.e("channer test", "View size: " + v_width + ", " + v_height);
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(v_width, v_height);
        layoutParams.setMargins(0, (int) ((metrics.heightPixels - v_height) / 2f + 0.5f), 0, 0);
        container.addView(cameraPreviewView, layoutParams);
        container.addView(faceView, layoutParams);
        setContentView(container);
    }
}
