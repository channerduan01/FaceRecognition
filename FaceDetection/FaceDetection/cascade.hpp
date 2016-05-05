//
//  cascade.hpp
//  FaceDetection
//
//  Created by Channer Duan on 4/25/16.
//  Copyright © 2016 Channer Duan. All rights reserved.
//

#ifndef cascade_hpp
#define cascade_hpp

#endif /* cascade_hpp */
#include <iostream>

class WeakClassifier {
public:
    int haar_type;
    bool has_trans;
    int x;
    int y;
    int w;
    int h;
    float a;
    int thre;
    bool best_p;
    
    void init(std::string description);
    void init(const WeakClassifier * const classifier);
    void scaled(float scale);
    void printInfo();
};

class Adaboost {
public:
    WeakClassifier * classifiers;
    float fadeFactor;
    int size;
    
    void init(int size, float fadeFactor);
    void printInfo();
};

class Cascade {
public:
    Adaboost * adaboosts;
    int size, w_width, w_height;
    
    static Cascade generate(std::string filename);
    Cascade() {}
    Cascade(int size, int w_width, int w_height);
    void printInfo();
    Cascade scaled(float scale);
};







