package com.cdd.detection;

import android.app.Application;
import com.cdd.detection.recognize.MatchEngine;
import com.cdd.detection.utils.FileEnvironment;

/**
 * Created by channerduan on 4/20/16.
 */
public class EarApplication extends Application {
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
        try {
            FileEnvironment.initAppDirectory(EarApplication.this);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        // initial whole matching engine
        MatchEngine.getInstance().init();
    }

}
