
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
def obtain_high_quality_face_data():
    (train_1, train_label_1) = loaddata('svm.train.normgrey')  
    (train_2, train_label_2) = loaddata('svm.test.normgrey')
    face_list = []
    for i in range(np.sum(train_label_1)):
        face_list.append(train_1[i])
    for i in range(np.sum(train_label_2)):
        face_list.append(train_2[i])
    print 'got: %d face images' %(len(face_list))
    return face_list
def obtain_non_face_data(non_face_path, window_size, non_face_restrict_bound = 5
    , min_gap_of_pixel = 10):
    non_face_files = os.listdir(non_face_path)[1:]
    non_face_restrict_range = (9*(window_size**2), 246*(window_size**2))
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
                if sum_ > non_face_restrict_range[0] and sum_ < non_face_restrict_range[1] \
                    and np.max(tmp) - np.min(tmp) > min_gap_of_pixel:
    #                non_face_list.append('(%d,%d)' %(i,j))
                    non_face_list.append(tmp)
                base_y = base_y + window_size
            base_x = base_x + window_size
            base_y = image_y%window_size/2 + non_face_restrict_bound
    print 'got: %d non-face images (from %d)' %(len(non_face_list), total_num)
    #drawFigures(non_face_list[100000:100040],8)    
    return non_face_list
def loaddata(file_path):
    tmp = genfromtxt(open(file_path,'r'), delimiter=' ', dtype='f8', skip_header=2)
    data = tmp[:,0:-1]
    window_size = int(np.sqrt(data.shape[1]))
    data = data.reshape((len(data),window_size,window_size))
    label = tmp[:,-1]>0
    return (data, label)
def outputData(filename, (s_face,e_face), (s_non_face,e_non_face)):
    res = np.zeros((e_face+e_non_face-s_face-s_non_face, window_size**2 + 1), np.float)
    with Timer() as t:
        for i in range(e_face-s_face):
            res[i,:-1] = face_list[i+s_face].reshape(-1)
            max_ = np.max(res[i,:-1])
            min_ = np.min(res[i,:-1])
            res[i,:-1] = (res[i,:-1]-min_)/(max_-min_)
            res[i,-1] = 1
        gap_ = e_face - s_face
        for i in range(e_non_face-s_non_face):
            res[i+gap_,:-1] = non_face_list[s_non_face+i].reshape(-1)
            max_ = np.max(res[i,:-1])
            min_ = np.min(res[i,:-1])
            res[i,:-1] = (res[i,:-1]-min_)/(max_-min_)
            res[i+gap_,-1] = 0
        savetxt(filename, res, delimiter=' ', header='%d\n%d' %(e_face+e_non_face-s_face-s_non_face, window_size**2), fmt='%f', comments='')
    print 'output training dataset: %.2fs' % t.secs

plt.gray()
face_path = './Caltech_CropFaces/'
non_face_path = './train_non_face_scenes/'
window_size = 24
#------------------------------------------------ obtain the data
if not 'face_list' in dir() or not 'non_face_list' in dir() or face_list is None: 
    with Timer() as t:
        face_list = obtain_face_data(face_path, window_size)
#        face_list = obtain_high_quality_face_data()
        non_face_list = obtain_non_face_data(non_face_path, window_size)
        np.random.shuffle(face_list)
        np.random.shuffle(non_face_list)
    print 'obtain face data: %.2fs' % t.secs
#------------------------------------------------ output part of data
outputData('face_train_positive.csv', (0,4000), (0,0))
outputData('face_train_negtive.csv', (0,0), (3000,50000))
outputData('face_test.csv', (4000,6700), (0,3000))









