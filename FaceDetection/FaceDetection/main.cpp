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
int columns = 0;
int calcuRectangle(int x_end, int y_end, int w, int h) {
    return ii[y_end*columns+x_end]
    + ii[(y_end-h)*columns+x_end-w]
    - ii[y_end*columns+x_end-w]
    - ii[(y_end-h)*columns+x_end];
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
int calcuSquare4Haar(bool is_trans, int x, int y, int w, int h) {
    return calcuRectangle(x, y, w/2, h/2) + calcuRectangle(x-w/2, y-h/2, w/2, h/2) \
    - calcuRectangle(x-w/2, y, w/2, h/2) + calcuRectangle(x, y-h/2, w/2, h/2);
}

void init(int rows, int cols) {
    integralspace = new Matrix(rows+1, cols+1);
    integralspace_tmp = new Matrix(rows, cols);
    ii = integralspace->p_data;
    columns = integralspace->cols;
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
    Matrix * res = new Matrix(integralspace_tmp->rows, integralspace_tmp->cols);
    int i, j;
    for (i = 1;i < integralspace->rows;i++)
        for (j = 1;j < integralspace->cols;j++)
            *(res->p_data+(i-1)*res->cols+j-1) = calcuRectangle(j,i,1,1);
    return res;
}

bool checkByCascade(Cascade & cascade) {
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
//                    p_weak->printInfo();
//                    cout<<value<<endl;
                    break;
                case 1:
                    value = calcuGap3Haar(p_weak->has_trans, p_weak->x, p_weak->y, p_weak->w, p_weak->h);
                    break;
                default:
                    value = calcuSquare4Haar(p_weak->has_trans, p_weak->x, p_weak->y, p_weak->w, p_weak->h);
                    break;
            }
//            cout << "   " << p_weak->haar_type <<":" << value << endl;
            if (p_weak->best_p) {
                if (value <= p_weak->thre) res_sum += p_weak->a;
            } else {
                if (value > p_weak->thre) res_sum += p_weak->a;
            }
        }
        cout << res_sum << " " << a_sum << endl;
        if (res_sum < 0.5f * a_sum * p_ada->fadeFactor)
            return false;
    }
    return true;
}




string cascade_filename = "/Users/channerduan/Documents/study/Face_Recog/Haar/cascade_my_own_data_face";

int main(int argc, char** argv) {
    Cascade cascade = Cascade::generate(cascade_filename);
    cascade = cascade.scaled(16);
//    cascade.printInfo();
    
    Mat img = imread("/Users/channerduan/Documents/study/Face_Recog/Haar/temp52.jpg", CV_LOAD_IMAGE_UNCHANGED);
    init(img.rows, img.cols);
    Mat grayImg;
    cvtColor(img, grayImg, cv::COLOR_BGR2GRAY);
    Matrix input(grayImg);
    calcuIntegral(&input);
    
    checkByCascade(cascade);
    
//    int sum_ = 0;
//    int i, j;
//    int max_rows = integralspace->rows-cascade.w_height;
//    int max_cols = integralspace->cols-cascade.w_width;
//    for (i = 0;i < max_rows;i+=2) {
//        for (j = 0;j < max_cols;j+=2) {
//            if (checkByCascade(cascade)) sum_++;
//        }
//    }
//    cout << sum_ <<endl;
    
    Mat res = *reverseIntegral()->getMatForm();
//    cout << integralspace->getSum() << endl;
    
    namedWindow("MyWindow", CV_WINDOW_AUTOSIZE);
    imshow("MyWindow", res);
    waitKey(0);
    destroyWindow("MyWindow");

    
    
//    for (int i = 0;i < 1000;i++) {
//        Matrix a(3, 3);
//        a.printout();
//    }
    
    return 0;
}






