package com.channer.ear;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.util.List;

public class MyActivity extends Activity {

    Camera mCamera = null;
    CameraPreview mPreview = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCamera = getCameraInstance(this);
        Camera.Parameters parameters;
        parameters = mCamera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        int c_width = 0;
        int c_height = 0;
        for (int i = 0;i < supportedPreviewSizes.size();i++) {
//            Log.e("channer test", "Camera supported size: "
//                    + supportedPreviewSizes.get(i).width + ", "
//                    + supportedPreviewSizes.get(i).height);
            if (c_width == 0 && supportedPreviewSizes.get(i).height > 400) {
                c_width = supportedPreviewSizes.get(i).width;
                c_height = supportedPreviewSizes.get(i).height;
            }
        }
        parameters.setPreviewSize(c_width, c_height);
        mCamera.setParameters(parameters);
        Log.e("channer test", "Camera size: " + c_width + ", " + c_height);

        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        Log.e("channer test", "DisplayMetrics size: " + metrics.widthPixels + ", " + metrics.heightPixels);
        int v_width = metrics.widthPixels;
        int v_height = (int) (metrics.widthPixels * (float) c_width / (float) c_height + 0.5f);
        Log.e("channer test", "View size: " + v_width + ", " + v_height);
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(v_width, v_height);
        layoutParams.setMargins(0, (int) ((metrics.heightPixels - v_height) / 2f + 0.5f), 0, 0);
        preview.addView(mPreview, layoutParams);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(Context context) {
        Camera c = null;
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("channer test", "you don't has camera");
            return null;
        }
        int numberOfCamera = Camera.getNumberOfCameras();
        Log.e("channer test", "number of camera: " + String.valueOf(numberOfCamera));
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int preferId = 0;
        for (int i = 0; i < numberOfCamera; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            Log.e("channer test", "camera" + i
                    + ", is front: " + String.valueOf(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    + ", orientation: " + cameraInfo.orientation
                    + ", disable the sound: " + cameraInfo.canDisableShutterSound);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) preferId = i;
        }

        try {
            c = Camera.open(preferId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}
