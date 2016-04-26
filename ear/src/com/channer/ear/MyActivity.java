package com.channer.ear;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.io.File;
import java.util.List;

public class MyActivity extends Activity implements CameraPreview.CapturedCallback,
        View.OnClickListener {

    private Camera mCamera = null;
    private CameraPreview mPreview = null;

    private ImageView mButtonImageView = null;
    private TextView mButtonTextView = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initCamera();
        initUI();
        resetRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean mRecordFlag = false;
    private int mRecordNum = 0;

    private void resetRecord() {
        mRecordFlag = false;
        mPreview.capture("");
        mButtonImageView.setImageResource(R.drawable.ic_passive);
    }

    private void startRecord() {
        final EditText et = new EditText(this);
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle("Sampling name")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(getApplicationContext(), "Please input the name" + input, Toast.LENGTH_LONG).show();
                        } else {
                            File destDir = new File(FileEnvironment.getTmpImagePath() + input);
                            if (destDir.exists()) {
                                Toast.makeText(getApplicationContext(), "The '" + input + "' already sampled!", Toast.LENGTH_LONG).show();
                            } else {
                                destDir.mkdirs();
                                mRecordFlag = true;
                                mRecordNum = 0;
                                mButtonTextView.setText("");
                                mPreview.capture(input);
                                mButtonImageView.setImageResource(R.drawable.ic_active);
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        ad.show();
    }

    private void initUI() {
        mButtonImageView = (ImageView) findViewById(R.id.iv_btn);
        mButtonImageView.setOnClickListener(this);
        mButtonTextView = (TextView) findViewById(R.id.tv_btn);
    }

    private void initCamera() {
        mCamera = getCameraInstance(this);
        Camera.Parameters parameters;
        parameters = mCamera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        int c_width = 0;
        int c_height = 0;
        for (int i = 0; i < supportedPreviewSizes.size(); i++) {
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
        mPreview.setCallback(this);
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

    @Override
    public void callback() {
        if (mRecordFlag) {
            mRecordNum++;
            mButtonTextView.setText("" + mRecordNum + " got");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_btn:
                if (mRecordFlag) {
                    resetRecord();
                } else {
                    startRecord();
                }
                break;
        }

    }
}
