//
//  cascade.cpp
//  FaceDetection
//
//  Created by Channer Duan on 4/25/16.
//  Copyright © 2016 Channer Duan. All rights reserved.
//

#include "cascade.hpp"
#include <iostream>
#include <fstream>

void WeakClassifier::init(std::string description) {
    int tmp1, tmp2;
    sscanf(description.data(), "%d %d %d %d %d %d %f %d %d",
           &haar_type, &tmp1, &x, &y, &w, &h, &a, &thre, &tmp2);
    has_trans = tmp1;
    best_p = tmp2;
}
void WeakClassifier::printInfo() {
    std::cout << haar_type << " " << has_trans << " " << x << " " << y << " " << w << " " << h << " " << a << " " << thre << " " << best_p << std::endl;
}
void WeakClassifier::init(WeakClassifier * const p_cf) {
    this->haar_type = p_cf->haar_type;
    this->has_trans = p_cf->has_trans;
    this->x = p_cf->x;
    this->y = p_cf->y;
    this->w = p_cf->w;
    this->h = p_cf->h;
    this->a = p_cf->a;
    this->thre = p_cf->thre;
    this->best_p = p_cf->best_p;
}
void WeakClassifier::scaled(float scale) {
    if (scale == 1) return;
    this->x = (int)((float)this->x+this->w*(scale-1.0f)/2.0f+0.5f);
    this->y = (int)((float)this->y+this->h*(scale-1.0f)/2.0f+0.5f);
    this->w = (int)((float)this->w*scale+0.5f);
    this->h = (int)((float)this->h*scale+0.5f);
    this->thre = (int)((float)this->thre*scale+0.5f);
}

void Adaboost::init(int size, float fadeFactor) {
    this->size = size;
    this->classifiers = new WeakClassifier[size];
    this->fadeFactor = fadeFactor;
}
void Adaboost::printInfo() {
    std::cout << "Ada size-" << size << " (fade: " << fadeFactor << ")" << std::endl;
    for (int i = 0;i < size;i++)
        (classifiers+i)->printInfo();
}

Cascade Cascade::generate(std::string filename) {
    std::fstream fin(filename);
    std::string strLine;
    getline(fin,strLine);
    int cascadeLen, w_width, w_height;
    sscanf(strLine.data(), "%d %d %d", &cascadeLen, &w_width, &w_height);
    Cascade cascade(cascadeLen, w_width, w_height);
    int i, j;
    for (i = 0;i < cascadeLen;i++) {
        getline(fin,strLine);
        int adaLen;
        float fadeFactor;
        sscanf(strLine.data(), "%d %f", &adaLen, &fadeFactor);
        (cascade.adaboosts+i)->init(adaLen, fadeFactor);
        for (j = 0;j < adaLen;j++) {
            getline(fin,strLine);
            (cascade.adaboosts+i)->classifiers[j].init(strLine);
        }
    }
    return cascade;
}
Cascade::Cascade(int size, int w_width, int w_height) {
    this->size = size;
    this->adaboosts = new Adaboost[size];
    this->w_width = w_width;
    this->w_height = w_height;
}
void Cascade::printInfo() {
    std::cout << "Cascade size-" << size << " (w_width: " << w_width << ", w_height: " << w_height << ")" << std::endl;
    for (int i = 0;i < size;i++)
        (this->adaboosts+i)->printInfo();
    std::cout << std::endl;
}

Cascade Cascade::scaled(float scale) {
    Cascade cascade_new(this->size,
                        (int)((float)this->w_width*scale+0.5f),
                        (int)((float)this->w_height*scale+0.5f));
    int i, j;
    Adaboost * p_ada, * p_ada_new;
    WeakClassifier * p_weak, * p_weak_new;
    for (i = 0;i < this->size;i++) {
        p_ada = this->adaboosts + i;
        p_ada_new = cascade_new.adaboosts + i;
        p_ada_new->init(p_ada->size, p_ada->fadeFactor);
        
        p_weak = p_ada->classifiers;
        p_weak_new = p_ada_new->classifiers;
        for (j = 0;j < p_ada->size;j++, p_weak++, p_weak_new++) {
            p_weak_new->init(p_weak);
            p_weak_new->scaled(scale);
        }
    }
    return cascade_new;
}




