package com.cdd.soton.utils;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

/**
 * Created by channerduan on 5/5/16.
 */
public class CameraUtils {

    private static int RESOLUTION_SELECT_THRESHOLD = 200;

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
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
//            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) preferId = i;
            if (cameraInfo.facing != Camera.CameraInfo.CAMERA_FACING_FRONT) preferId = i;

        }

        try {
            c = Camera.open(preferId); // attempt to get a Camera instance
        } catch (Exception e) {
            return null;
        }
        Camera.Parameters parameters;
        parameters = c.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        int c_width = 0;
        int c_height = 0;
        for (int i = 0; i < supportedPreviewSizes.size(); i++) {
            Log.e("channer test", "channer test Camera supported size: "
                    + supportedPreviewSizes.get(i).width + ", "
                    + supportedPreviewSizes.get(i).height);
            if (supportedPreviewSizes.get(i).height > RESOLUTION_SELECT_THRESHOLD) {
                c_width = supportedPreviewSizes.get(i).width;
                c_height = supportedPreviewSizes.get(i).height;
                break;
            }
        }
        parameters.setPreviewSize(c_width, c_height);
        c.setParameters(parameters);
        return c;
    }
}
