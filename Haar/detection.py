
import os  
from PIL import Image
import numpy as np
import matplotlib.pyplot as plt
import cv2
from numba import double
from numba.decorators import jit, autojit
from timer import Timer
from numpy import genfromtxt, savetxt

def readImage(path):
    '''Read image as np array, from path'''
    im = np.array(Image.open(path).convert('L'))
    return im


def calcuRectangle(ii, x_end, y_end, w, h):
    return ii[y_end,x_end] + ii[y_end-h,x_end-w] - ii[y_end,x_end-w] - ii[y_end-h,x_end]
def calcuBasis2Haar(ii,is_trans,x,y,w,h):
    if not is_trans:
        return calcuRectangle(ii, x, y, w, h/2) - calcuRectangle(ii, x, y-h/2, w, h/2)
    else:
        return calcuRectangle(ii, y, x, h/2, w) - calcuRectangle(ii, y-h/2, x, h/2, w)
def calcuGap3Haar(ii,is_trans,x,y,w,h):
    if not is_trans:
        return calcuRectangle(ii, x, y, w/3, h) + calcuRectangle(ii, x-2*w/3, y, w/3, h) - 2*calcuRectangle(ii, x-w/3, y, w/3, h)
    else:
        return calcuRectangle(ii, y, x, h, w/3) + calcuRectangle(ii, y, x-2*w/3, h, w/3) - 2*calcuRectangle(ii, y, x-w/3, h, w/3)
def calcuSquare4Haar(ii,x,y,w,h):
    return calcuRectangle(ii, x, y, w/2, h/2) + calcuRectangle(ii, x-w/2, y-h/2, w/2, h/2) \
        - calcuRectangle(ii, x-w/2, y, w/2, h/2) + calcuRectangle(ii, x, y-h/2, w/2, h/2)
def calcuSingleHaar(ii, (haar_type, has_trans, x, y, w, h)):
    if haar_type == 0:
        return calcuBasis2Haar(ii, has_trans, x, y, w, h)
    elif haar_type == 1:
        return calcuGap3Haar(ii, has_trans, x, y, w, h)
    else:
        return calcuSquare4Haar(ii, x, y, w, h)
        
def calcuSpecificHaarFeatures(iis, (haar_type, has_trans, x, y, w, h)):
    size = len(iis)
    features = np.zeros((size), np.int)
    for i in range(size):
        features[i] = calcuSingleHaar(iis[i], (haar_type, has_trans, x, y, w, h))
    return features
def checkByHaarWeakClassifier(values, p_, thre):
    if p_:
        return values <= thre
    else:
        return values > thre
def fastCheckByCascade(cascade, image):
    for i in range(len(cascade)):
        adaboost, fade_param = cascade[i] 
        res_ = 0.0
        a_sum = 0.0
        for (a,thre,best_k,best_p) in adaboost:
            a_sum = a_sum + a
            res_ = res_ + a*checkByHaarWeakClassifier(calcuSingleHaar(image,haar_map[best_k]),best_p,thre)
        if res_ < 0.5*a_sum*fade_param: return False
    return True

plt.gray()
#image = readImage('detection.py')

#plt.imshow(image)

#window_size = 19
#for i in range(window_size,image.shape[0]):
#    for j in range(window_size,image.shape[1]):
#        data_[num_] = image[i-window_size:i+1,j-window_size:j+1]
#        



#def saveCascade11(filename, cascade):
#    np.save(filename, cascade)
#def loadCascade11(filename):
#    return np.load(filename).tolist()


cascade = cascade_
new_cascade = []
for layer in cascade:
    (haar_type, has_trans, x, y, w, h) = haar_map[layer[0][2]]



#aa = loadCascade(filename)



















