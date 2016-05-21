package com.cdd.detection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.cdd.detection.imagebasis.Rect;
import com.cdd.detection.matching.DataEngine;
import com.cdd.detection.utils.CameraUtils;
import com.cdd.detection.utils.FileEnvironment;
import com.cdd.detection.utils.ImgUtils;
import com.cdd.detection.views.CameraPreviewView;
import com.cdd.detection.views.PhotoFrameView;
import com.googlecode.javacv.cpp.opencv_core;

import java.io.IOException;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

public class ShootActivity extends Activity implements DetectEngine.CapturedCallback {

    public static final int MATCH_CON_THRESHOLD = 3;

    public static final String INTENT_KEY_COLLECT_NUMBER = "INTENT_KEY_COLLECT_NUMBER";
    public static final String INTENT_KEY_COLLECT_SUBJECT_NAME = "INTENT_KEY_COLLECT_SUBJECT_NAME";

    private boolean mIsSampleMode = false;
    // params for sample
    private int mDetectTargetNumber = 0;
    private String mSubjectName = "";
    // params for verify
    private int mMatchConsNum = 0;
    private String mMatchPrevious = "";

    private PhotoFrameView photoFrameView;
    private CameraPreviewView cameraPreviewView;

    private DetectEngine detectEngine;

    private TextView fpsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoot_layout);
        mSubjectName = getIntent().getStringExtra(INTENT_KEY_COLLECT_SUBJECT_NAME);
        if (TextUtils.isEmpty(mSubjectName)) {
            mIsSampleMode = false;
        } else {
            mIsSampleMode = true;
            mDetectTargetNumber = getIntent().getIntExtra(INTENT_KEY_COLLECT_NUMBER, 0);
        }
        try {
            initCamera();
            initUI();
        } catch (IOException e) {
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
            finish();
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

    private float frameNumber = 0;
    private long startTimestampe = System.currentTimeMillis();


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
            opencv_core.IplImage image = ImgUtils.resize(ImgUtils.crop(data, rectList.get(0)),
                    new opencv_core.CvSize(DataEngine.FORM_WIDTH, DataEngine.FORM_HEIGHT));
            if (!mIsSampleMode) {
                String match = DataEngine.getInstance().matching(image);
                if (match.equals("") || !match.equals(mMatchPrevious)) {
                    mMatchPrevious = match;
                    mMatchConsNum = 1;
                } else {
                    mMatchConsNum++;
                    if (mMatchConsNum >= MATCH_CON_THRESHOLD) {
                        Intent intent = new Intent();
                        intent.putExtra(INTENT_KEY_COLLECT_SUBJECT_NAME, mMatchPrevious);
                        setResult(Activity.RESULT_OK, intent);
                        detectEngine.setCallback(null);
                        finish();
                    }
                }
            } else {
                cvSaveImage(FileEnvironment.getTmpImagePath() + "/" + mSubjectName +
                        "/" + String.valueOf(System.currentTimeMillis()) + ".jpg", image);
                mDetectTargetNumber--;
                if (mDetectTargetNumber <= 0) {
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_KEY_COLLECT_SUBJECT_NAME, mSubjectName);
                    setResult(Activity.RESULT_OK, intent);
                    detectEngine.setCallback(null);
                    finish();
                }
            }
        }
    }
}
