package com.cdd.detection.imagebasis;

import com.googlecode.javacv.cpp.opencv_core;

/**
 * Created by channerduan on 5/5/16.
 */
public class Rect implements Comparable<Rect> {
    public int s_x;
    public int s_y;
    public int w;
    public int h;

    public Rect(int x, int y, int w, int h) {
        this.s_x = x;
        this.s_y = y;
        this.w = w;
        this.h = h;
    }

    public Rect(float x, float y, float w, float h) {
        this.s_x = (int) (x + 0.5f);
        this.s_y = (int) (y + 0.5f);
        this.w = (int) (w + 0.5f);
        this.h = (int) (h + 0.5f);
    }

    public opencv_core.CvRect trans2CvRect() {
        return new opencv_core.CvRect(s_x, s_y, w, h);
    }

    @Override
    public int compareTo(Rect o) {
        return w * h - o.w * o.h;
    }
}