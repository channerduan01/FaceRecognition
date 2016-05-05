
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
def change_width(img,size):
    return cv2.resize(img,(size,size),interpolation=cv2.INTER_CUBIC)
def calcuIntegral(img):
    s = np.zeros_like(img,dtype=np.int)
    s[:,0] = img[:,0]
    for i in range(1,s.shape[1]):
        s[:,i] = s[:,i-1] + img[:,i]
    ii = np.zeros((s.shape[0]+1,s.shape[1]+1),dtype=np.int)
    ii[1,1:] = s[0,:]
    for i in range(1,s.shape[0]):
        ii[i+1,1:] = ii[i,1:] + s[i,:]
    return ii
def reverseIntegral(ii):
    image = np.zeros((ii.shape[0]-1,ii.shape[1]-1),dtype=np.int)
    for x in range(1, ii.shape[0]):
        for y in range(1, ii.shape[1]):
            image[x-1,y-1] = calcuRectangle(ii,y,x,1,1)
    return image

def calcuRectangle(ii, x_end, y_end, w, h):
#    print '%d,%d,%d,%d' %(x_end, y_end, w, h)
#    print '%d,%d,%d,%d' %(ii[y_end,x_end],ii[y_end-h,x_end-w],ii[y_end,x_end-w],ii[y_end-h,x_end])
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
        
def fastCheckByCascade(cascade, image):
    for i in range(len(cascade)):
        adaboost, fade_param = cascade[i] 
        res_ = 0.0
        a_sum = 0.0
        for (haar_type, has_trans, x, y, w, h, a, thre, best_p) in adaboost:
            a_sum = a_sum + a
            if haar_type == 0:
                value = calcuBasis2Haar(image, has_trans, x, y, w, h)
            elif haar_type == 1:
                value = calcuGap3Haar(image, has_trans, x, y, w, h)
            else:
                value = calcuSquare4Haar(image, x, y, w, h)
#                print "%d %d %d %d %d %d %f %d %d" %(haar_type, has_trans, x, y, w, h, a, thre, best_p)
#                print "%d" %value
            if best_p:
                if value <= thre: res_ = res_ + a
            else:
                if value > thre: res_ = res_ + a
#        print "!%f %f" %(res_, a_sum)
        if res_ < 0.5*a_sum*fade_param:
            return False
    return True
fastCheckByCascade = autojit(fastCheckByCascade)

def covertCascade(cascade):
    new_cascade = []
    for layer in cascade:
        new_layer = []
        for ada in layer[0]:
            (haar_type, has_trans, x, y, w, h) = haar_map[ada[2]]
            new_layer.append((haar_type, has_trans, x, y, w, h, ada[0], ada[1], ada[3]))
        new_cascade.append((new_layer, layer[1]))   
    return new_cascade

def scaledCascade(cascade, scale_factor):
    new_cascade = []
    for layer in cascade:
        new_layer = []
        for ada in layer[0]:
            (haar_type, has_trans, x, y, w, h, a, thre, best_p) = ada
            x = int(np.round(x * scale_factor))
            y = int(np.round(y * scale_factor))
            w = int(np.round(w * scale_factor))      
            h = int(np.round(h * scale_factor))      
            thre = thre * scale_factor
            new_layer.append((haar_type, has_trans, x, y, w, h, a, thre, best_p))
        new_cascade.append((new_layer, layer[1]))   
    return new_cascade    

def detect(image_integral, cascade):
    base_window_size = 24.0
    scale_factor = 7
    cas_ = scaledCascade(cascade,scale_factor)
    window_size = int(np.round(base_window_size * scale_factor))
    list_ = []
    mean_i = 0
    mean_j = 0
    for i in range(window_size,image_integral.shape[0],2):
        for j in range(window_size,image_integral.shape[1],2):
            if fastCheckByCascade(cas_, image_integral[i-window_size:i+1,j-window_size:j+1]):
                list_.append((i,j,window_size,window_size))
                mean_i = mean_i + i
                mean_j = mean_j + j
    print '%d' %len(list_)
    if len(list_) == 0:
        return list_, (0, 0, 0, 0)
    else:
        return list_, (int(np.round(mean_i/len(list_))), int(np.round(mean_j/len(list_))), window_size, window_size)


def outputCascade(cascade, w, h):
    f=file("cascade_my_own_data_face","w+")
    
    f.writelines("%d %d %d\n" %(len(cascade), w, h))
    for i in range(len(cascade)):
        layer = cascade[i]
        f.writelines("%d %f\n" %(len(layer[0]), layer[1]))
        for cf in layer[0]:
            f.writelines("%d %d %d %d %d %d %f %d %d\n" %cf)
    f.close()

plt.gray()
#image = readImage('my_profile.jpg')
#image = readImage('faculty_roster.jpg')
image = readImage('temp52.jpg')

#image = cv2.resize(image,(150,200), interpolation=cv2.INTER_CUBIC)
#image = cv2.resize(image,(750,340), interpolation=cv2.INTER_CUBIC)
#image = cv2.resize(image,(440,700), interpolation=cv2.INTER_CUBIC)

#image[11:11+60,81:81+60] = change_width(reverseIntegral(train_data[1]).astype(np.float),60)
image_integral = calcuIntegral(image)
#plt.imshow(image)

#fastCheckByCascade(scaledCascade(cascade,16), image_integral)

#print '%d' %calcuSquare4Haar(image_integral, 28, 157, 32, 288)
#print '%d' %calcuRectangle(image_integral, 28,13,16,144)

cascade = covertCascade(np.load('cascade_my_own_data_face_test.npy').tolist())
list_, coor = detect(image_integral, cascade)

image_show = cv2.cvtColor(image,cv2.COLOR_GRAY2RGB)
#for coor in list_:
#    cv2.rectangle(image_show,(coor[1]-coor[3],coor[0]-coor[2]),(coor[1],coor[0]),(255,0,0),2)
if len(list_) != 0:
    cv2.rectangle(image_show,(coor[1]-coor[3],coor[0]-coor[2]),(coor[1],coor[0]),(255,0,0), 3)
plt.figure()
plt.imshow(image_show)



#outputCascade(cascade, window_size, window_size)











