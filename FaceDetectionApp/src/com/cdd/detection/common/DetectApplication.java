package com.cdd.detection.common;

import android.app.Application;
import com.cdd.detection.utils.FileEnvironment;

/**
 * Created by channerduan on 4/20/16.
 */
public class DetectApplication extends Application {
    private static Application instance;

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initApp();
    }

    private void initApp() {
        FileEnvironment.initAppDirectory(DetectApplication.this);
    }


}
