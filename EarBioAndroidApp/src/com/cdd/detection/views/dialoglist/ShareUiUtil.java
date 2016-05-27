package com.cdd.detection.views.dialoglist;

import android.content.Context;

public class ShareUiUtil {

    private static float mDensity = 0;

    public static int dip2px(int dip, Context context) {
        return (int) (0.5F + getDensity(context) * dip);
    }

    private static float getDensity(Context context) {
        if (mDensity == 0) {
            mDensity = context.getResources().getDisplayMetrics().density;
        }
        return mDensity;
    }
}
