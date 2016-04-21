//
//  main.cpp
//  FaceDetection
//
//  Created by Channer Duan on 4/20/16.
//  Copyright © 2016 Channer Duan. All rights reserved.
//

#include <iostream>

#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"

using namespace cv;
using namespace std;

int main(int argc, char** argv) {
    Mat img = imread("/Users/channerduan/Desktop/temp44.jpg", CV_LOAD_IMAGE_UNCHANGED);
    if (img.empty()) //check whether the image is loaded or not
    {
        cout << "Error : Image cannot be loaded..!!" << endl;
        //system("pause"); //wait for a key press
        return -1;
    }
    namedWindow("MyWindow", CV_WINDOW_AUTOSIZE);
    imshow("MyWindow", img);
    waitKey(0);
    destroyWindow("MyWindow");
    return 0;
}