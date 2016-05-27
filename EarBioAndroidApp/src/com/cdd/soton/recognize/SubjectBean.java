package com.cdd.soton.recognize;

import com.googlecode.javacv.cpp.opencv_core;

import java.util.List;

/**
 * Created by channerduan on 5/21/16.
 */
public class SubjectBean {

    public String name;
    public List<opencv_core.CvMat> listProjectOnBase;


    public SubjectBean(String name, List<opencv_core.CvMat> listProjectOnBase) {
        this.name = name;
        this.listProjectOnBase = listProjectOnBase;
    }

}
