//
//  main.cpp
//  FaceDetection
//
//  Created by Channer Duan on 4/20/16.
//  Copyright © 2016 Channer Duan. All rights reserved.
//

#include <iostream>
#include <fstream>
#include "cascade.hpp"

#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

using namespace cv;
using namespace std;


void printMat(Mat mat, string mark) {
    cout<<"Mat ->"<<mark<<"<- "<<mat.type()<<endl;
    cout<<"shape: "<<mat.rows<<", "<<mat.cols<<endl;
    cout<<"dtype: "<<mat.type()<<endl;
    cout<<"data: "<<mat.dataend-mat.datastart<<endl;
    cout<<endl;
}

class Matrix {
public:
    int * p_data;
    int rows;
    int cols;
    int size;
    Matrix(int rows, int cols) {
        p_data = (int *) malloc(rows*cols*sizeof(int));
        this->rows = rows;
        this->cols = cols;
        size = this->rows*this->cols;
    }
    Matrix(Mat greyScale) {
        this->rows = greyScale.rows;
        this->cols = greyScale.cols;
        size = this->rows*this->cols;
        p_data = new int[size];
        for (int i = 0;i < size;i++) {
            *(p_data+i) = *(greyScale.data+i);
        }
    }
    ~Matrix() {
        free(p_data);
        p_data = NULL;
    }
    Mat * getMatForm() {
        char * tmp_data = new char[size];
        for (int i = 0;i < size;i++)
            *(tmp_data+i) = *(p_data+i);
        Mat * p = new Mat(Size(this->cols,this->rows), CV_8U, (void*) tmp_data);
        delete tmp_data;
        return p;
    }
    void reset() {
        memset(p_data, 0, rows*cols);
    }
    long getSum() {
        long sum = 0;
        for (int i = 0;i < size;i++)
            sum += *(p_data+i);
        return sum;
    }
    void printout() {
        int i, j;
        int * p_ = p_data;
        for (i = 0;i < rows;i++) {
            for (j = 0;j < cols;j++)
                cout << *(p_+j) << " ";
            p_ += cols;
            cout << endl;
        }
    }
};


Matrix * integralspace = NULL;  // calculating space
Matrix * integralspace_tmp = NULL;
int * ii;
int x_shift, y_shift, columns;
int calcuRectangle(int x_end, int y_end, int w, int h) {
    return ii[(y_end+y_shift)*columns+x_end+x_shift]
    + ii[(y_end-h+y_shift)*columns+x_end-w+x_shift]
    - ii[(y_end+y_shift)*columns+x_end-w+x_shift]
    - ii[(y_end-h+y_shift)*columns+x_end+x_shift];
}
int calcuBasis2Haar(bool is_trans, int x, int y, int w, int h) {
    if (!is_trans)
        return calcuRectangle(x, y, w, h/2) - calcuRectangle(x, y-h/2, w, h/2);
    else
        return calcuRectangle(y, x, h/2, w) - calcuRectangle(y-h/2, x, h/2, w);
}
int calcuGap3Haar(bool is_trans, int x, int y, int w, int h) {
    if (!is_trans)
        return calcuRectangle(x, y, w/3, h) + calcuRectangle(x-2*w/3, y, w/3, h)
        - 2*calcuRectangle(x-w/3, y, w/3, h);
    else
        return calcuRectangle(y, x, h, w/3) + calcuRectangle(y, x-2*w/3, h, w/3) - 2*calcuRectangle(y, x-w/3, h, w/3);
}
int calcuSquare4Haar(int x, int y, int w, int h) {
    return calcuRectangle(x, y, w/2, h/2) + calcuRectangle(x-w/2, y-h/2, w/2, h/2) - calcuRectangle(x-w/2, y, w/2, h/2) + calcuRectangle(x, y-h/2, w/2, h/2);
}

void init(int rows, int cols) {
    integralspace = new Matrix(rows+1, cols+1);
    integralspace_tmp = new Matrix(rows, cols);
    ii = integralspace->p_data;
    columns = integralspace->cols;
    x_shift = y_shift = 0;
}
void calcuIntegral(Matrix * img) {
    integralspace_tmp->reset();
    int i, j;
    for (i = 0;i < img->rows;i++)
        *(integralspace_tmp->p_data+i*img->cols) = *(img->p_data+i*img->cols);
    for (j = 1;j < img->cols;j++)
        for (i = 0;i < img->rows;i++)
            *(integralspace_tmp->p_data+i*img->cols+j) =
            *(integralspace_tmp->p_data+i*img->cols+j-1) +
            *(img->p_data+i*img->cols+j);
    integralspace->reset();
    for (j = 0;j < img->cols;j++)
        *(integralspace->p_data+img->cols+1+j+1) = *(integralspace_tmp->p_data+j);
    for (i = 1;i < img->rows;i++)
        for (j = 0;j < img->cols;j++)
            *(integralspace->p_data+(i+1)*(img->cols+1)+j+1) =
            *(integralspace->p_data+(i)*(img->cols+1)+j+1) +
            *(integralspace_tmp->p_data+i*img->cols+j);
}
Matrix * reverseIntegral() {
    x_shift = y_shift = 0;
    Matrix * res = new Matrix(integralspace_tmp->rows, integralspace_tmp->cols);
    int i, j;
    for (i = 1;i < integralspace->rows;i++)
        for (j = 1;j < integralspace->cols;j++)
            *(res->p_data+(i-1)*res->cols+j-1) = calcuRectangle(j,i,1,1);
    return res;
}

bool checkByCascade(const Cascade & cascade) {
    int i, j, value;
    float res_sum, a_sum;
    Adaboost * p_ada;
    WeakClassifier * p_weak;
    for (i = 0;i < cascade.size;i++) {
        res_sum = a_sum = 0;
        p_ada = cascade.adaboosts + i;
        p_weak = p_ada->classifiers;
        for (j = 0;j < p_ada->size;j++, p_weak++) {
            a_sum += p_weak->a;
            switch (p_weak->haar_type) {
                case 0:
                    value = calcuBasis2Haar(p_weak->has_trans, p_weak->x, p_weak->y, p_weak->w, p_weak->h);
                    break;
                case 1:
                    value = calcuGap3Haar(p_weak->has_trans, p_weak->x, p_weak->y, p_weak->w, p_weak->h);
                    break;
                default:
                    value = calcuSquare4Haar(p_weak->x, p_weak->y, p_weak->w, p_weak->h);
                    break;
            }
            if (p_weak->best_p) {
                if (value <= p_weak->thre) res_sum += p_weak->a;
            } else {
                if (value > p_weak->thre) res_sum += p_weak->a;
            }
        }
        //        cout << res_sum << " " << a_sum << endl;
        if (res_sum < 0.5f * a_sum * p_ada->fadeFactor)
            return false;
    }
    return true;
}

class Evaluation {
public:
    int totalSum;
    float sXs;
    float sYs;
    float ws;
    float hs;
    
    int s_x, s_y, w, h;
    
    void reset() {
        totalSum = 0;
        sXs = sYs = ws = hs = 0;
    }
    void add(int sum, float start_x, float start_y, int w, int h) {
        if (sum) {
            sXs += start_x*sum;
            sYs += start_y*sum;
            ws += w*sum;
            hs += h*sum;
            totalSum += sum;
        }
    }
    bool check() {
        if (totalSum > 3000) {
            cout << totalSum << endl;
            s_x = (int) (sXs/totalSum+0.5f);
            s_y = (int) (sYs/totalSum+0.5f);
            w = (int) (ws/totalSum+0.5f);
            h = (int) (hs/totalSum+0.5f);
            return true;
        } else return false;
    }
};


string cascade_filename = "/Users/channerduan/Documents/study/Face_Recog/Haar/cascade_my_own_data_face";

string test_pic_filename = "/Users/channerduan/Documents/study/Face_Recog/Haar/temp52.jpg";
//string test_pic_filename = "/Users/channerduan/Documents/study/Face_Recog/Haar/my_profile.jpg";


#define PROPER_LAYER_NUM 5
#define LOWER_LIMIT 0.2f
#define UPPER_LIMIT 0.8f

Cascade cascades[PROPER_LAYER_NUM];
Evaluation evaluation;
Cascade cascade_base = Cascade::generate(cascade_filename);
void initCascade(int rows, int cols) {
    int lower_scale, upper_scale;
    if ((float)cols/cascade_base.w_width < (float)rows/cascade_base.w_height) {
        lower_scale = (float)cols/cascade_base.w_width*LOWER_LIMIT;
        upper_scale = (float)cols/cascade_base.w_width*UPPER_LIMIT;
    } else {
        lower_scale = (float)rows/cascade_base.w_height*LOWER_LIMIT;
        upper_scale = (float)rows/cascade_base.w_height*UPPER_LIMIT;
    }
    float search_gap = (float)(upper_scale-lower_scale)/(PROPER_LAYER_NUM-1);
    float scale = upper_scale;
    for (int i = 0;i < PROPER_LAYER_NUM;i++, scale -= search_gap) {
        cascades[i] = cascade_base.scaled(scale);
    }
}

void evaluateIntegral() {
    Cascade *p_cascade = cascades;
    evaluation.reset();
    for (int i = 0;i < PROPER_LAYER_NUM;i++, p_cascade++) {
        float x_face, y_face;
        int x_tmp, y_tmp;
        int sum_ = 0;
        int max_rows = integralspace->rows-cascades[i].w_height;
        int max_cols = integralspace->cols-cascades[i].w_width;
        x_tmp = y_tmp = 0;
        for (y_shift = 0;y_shift < max_rows;y_shift+=2)
            for (x_shift = 0;x_shift < max_cols;x_shift+=2)
                if (checkByCascade(cascades[i])) {
                    sum_++;
                    x_tmp += x_shift;
                    y_tmp += y_shift;
                }
        if (sum_) {
            x_face = (float) x_tmp/sum_;
            y_face =(float) y_tmp/sum_;
        }
        evaluation.add(sum_, x_face, y_face, cascades[i].w_width, cascades[i].w_height);
//        cout << sum_ << ": (" << x_face << ", " << y_face << ")" << endl;
        //        rectangle(img, Rect(x_face,y_face,cascades[i].w_width,cascades[i].w_height), Scalar(0,0,255), 2);
    }
}

int main(int argc, char** argv) {
    //    Mat img = imread(test_pic_filename, CV_LOAD_IMAGE_UNCHANGED);
    //    init(img.rows, img.cols);
    //    initCascade(img.rows, img.cols);
    //
    //    Mat grayImg;
    //    cvtColor(img, grayImg, cv::COLOR_BGR2GRAY);
    //    Matrix input(grayImg);
    //    calcuIntegral(&input);
    //
    //    long start = clock();
    //    evaluateIntegral();
    //    cout << "evaluation: " << evaluation.check() << endl;
    //    cout << "time: " << (double)(clock() - start) / CLOCKS_PER_SEC << "s" << endl;
    //
    //    Mat res = *reverseIntegral()->getMatForm();
    //    cvtColor(res, img, cv::COLOR_GRAY2BGR);
    //    rectangle(img, Rect(evaluation.s_x, evaluation.s_y, evaluation.w, evaluation.h), Scalar(0,255,255), 2);
    //
    //    namedWindow("MyWindow", CV_WINDOW_AUTOSIZE);
    //    imshow("MyWindow", img);
    //    waitKey(0);
    //    destroyWindow("MyWindow");
    
    char c;
    Mat frame;
    VideoCapture cam(0);
    if (!cam.isOpened())
        return -1;
    int width_resolution = 640, height_resolution = 480;
    cam.set(CV_CAP_PROP_FRAME_WIDTH,width_resolution);
    cam.set(CV_CAP_PROP_FRAME_HEIGHT,height_resolution);
    init(height_resolution, width_resolution);
    initCascade(height_resolution, width_resolution);
    namedWindow("camera",1);
    while(1)
    {
        cam >> frame;
        if(frame.empty()) return -1;
        Mat grayImg;
        cvtColor(frame, grayImg, cv::COLOR_BGR2GRAY);
        Matrix input(grayImg);
        calcuIntegral(&input);
        evaluateIntegral();
        cvtColor(grayImg, frame, cv::COLOR_GRAY2BGR);
//        rectangle(frame, Rect(0,0,100,100), Scalar(0,0,255), 3);
        if (evaluation.check())
            rectangle(frame, Rect(evaluation.s_x, evaluation.s_y, evaluation.w, evaluation.h), Scalar(0,255,255), 2);
        imshow("camera",frame);
        c = (char) waitKey(40);
        if(27 == c)
            break;
    }
    destroyWindow("camera");
    return 0;
}






