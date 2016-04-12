
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
def drawFigures(params,width=6):
    length = len(params)
    if (length < 2 or length%2 == 1 or width < 1):
        raise Exception("illegal input")
    for i in range(0,length,2):
        if (i%(width*2) == 0):
            plt.figure()
        plt.subplot(100+width*10+(i%(width*2))/2+1)
        plt.title(params[i])
        plt.axis('off')
        plt.imshow(params[i+1])
    return
def change_width(img,size):
    return cv2.resize(img,(size,size),interpolation=cv2.INTER_CUBIC)


def obtain_face_data(face_path, window_size):
    face_files = os.listdir(face_path)[1:]
    face_list = []
    for file_ in face_files:
        face_list.append(change_width(readImage(face_path+file_),window_size))
    print 'got: %d face images' %(len(face_list))  
    return face_list  
    
def obtain_non_face_data(non_face_path, window_size, non_face_restrict_bound = 10):
    non_face_files = os.listdir(non_face_path)[1:]
    non_face_restrict_range = (10*(window_size**2), 245*(window_size**2))
    total_num = 0
    non_face_list = []
    for file_ in non_face_files:
        image = readImage(non_face_path+file_)
#        print '(%d, %d)' %(image.shape[0], image.shape[1])
        total_num = total_num + image.shape[0]/window_size*image.shape[1]/window_size
        image_x = image.shape[0] - non_face_restrict_bound*2
        image_y = image.shape[1] - non_face_restrict_bound*2
        base_x = image_x%window_size/2 + non_face_restrict_bound
        base_y = image_y%window_size/2 + non_face_restrict_bound
        for i in range(image_x/window_size):
            for j in range(image_y/window_size):
    #            print '%d-%d, %d-%d' %(base_x,base_x+window_size,base_y,base_y+window_size)
                tmp = image[base_x:base_x+window_size,base_y:base_y+window_size]
                sum_ = np.sum(tmp)
                if sum_ > non_face_restrict_range[0] and sum_ < non_face_restrict_range[1]:
    #                non_face_list.append('(%d,%d)' %(i,j))
                    non_face_list.append(tmp)
                base_y = base_y + window_size
            base_x = base_x + window_size
            base_y = image_y%window_size/2 + non_face_restrict_bound
    print 'got: %d non-face images (from %d)' %(len(non_face_list), total_num)
    #drawFigures(non_face_list[100000:100040],8)    
    return non_face_list

plt.gray()
face_path = './Caltech_CropFaces/'
non_face_path = './train_non_face_scenes/'
window_size = 21
#------------------------------------------------ obtain the data
if not 'face_list' in dir() or not 'non_face_list' in dir(): 
    with Timer() as t:
        face_list = obtain_face_data(face_path, window_size)
        non_face_list = obtain_non_face_data(non_face_path, window_size)
    print 'obtain non-face data: %.2fs' % t.secs
#------------------------------------------------ selection part of data
num_select_face = 4500
num_select_non_face = 10500
res = np.zeros((num_select_face + num_select_non_face, window_size**2 + 1), np.float)
with Timer() as t:
    index = np.arange(len(face_list))
    np.random.shuffle(index)
    for i in range(num_select_face):
        res[i,:-1] = face_list[index[i]].reshape(-1)
        max_ = np.max(res[i,:-1])
        min_ = np.min(res[i,:-1])
        res[i,:-1] = (res[i,:-1]-min_)/(max_-min_)
        res[i,-1] = 1
    index = np.arange(len(non_face_list))
    np.random.shuffle(index)
    for i in range(num_select_non_face):
        res[i+num_select_face,:-1] = non_face_list[index[i]].reshape(-1)
        max_ = np.max(res[i,:-1])
        min_ = np.min(res[i,:-1])
        res[i,:-1] = (res[i,:-1]-min_)/(max_-min_)
        res[i+num_select_face,-1] = 0
    savetxt('face_train_channer.csv', res, delimiter=' ', header='%d\n%d' %(num_select_face + num_select_non_face, window_size**2), fmt='%f', comments='')
print 'output training dataset: %.2fs' % t.secs










