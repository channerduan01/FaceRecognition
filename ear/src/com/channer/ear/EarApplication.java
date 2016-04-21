package com.channer.ear;

import android.app.Application;

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
        FileEnvironment.initAppDirectory(EarApplication.this);
    }


}
