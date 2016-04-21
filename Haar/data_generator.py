
import glob  
from PIL import Image
import numpy as np
import matplotlib.pyplot as plt
#import cv2
from numba import double
from numba.decorators import jit, autojit
from timer import Timer
from numpy import genfromtxt, savetxt

def readImage(path):
    '''Read image as np array, from path'''
    im = np.array(Image.open(path).convert('L'))
    return im
    
class DataGenerator(object):
    def __init__(self, window_size, filepaths, bundle_size=200000, file_index=0, resize_factor = 0):
        self.file_list = []
        for path in filepaths:
            self.file_list.extend(glob.glob(path + '*.jpg'))
#        np.random.shuffle(self.file_list)
        self.resize_factor = resize_factor
        self.file_num = len(self.file_list)
        self.file_index = file_index
        self.image_index = (window_size, window_size) # start from this because of images are integral images
        self.window_size = window_size
        self.bundle_size = bundle_size
        
    @staticmethod
    def integral(img):
        s = np.zeros_like(img,dtype=np.int)
        s[:,0] = img[:,0]
        for i in range(1,s.shape[1]):
            s[:,i] = s[:,i-1] + img[:,i]
        ii = np.zeros((s.shape[0]+1,s.shape[1]+1),dtype=np.int)
        ii[1,1:] = s[0,:]
        for i in range(1,s.shape[0]):
            ii[i+1,1:] = ii[i,1:] + s[i,:]
        return ii
        
    def getFiles(self):
        return self.file_list
    
    def getDebugInfo(self):
        print 'file_index: %d/%d, image_index (%d, %d)' %(self.file_index, self.file_num, self.image_index[0], self.image_index[1])
        return self.file_index
        
    def getTotalNum(self):
        window_size = self.window_size
        num_ = 0
        for f_index in range(self.file_index, self.file_num):
            (i, j) = readImage(self.file_list[f_index]).shape
            num_ = num_ + (i - window_size)*(j - window_size)
        if self.resize_factor == 0:
            return num_
        else:
            return self.resize_factor**2 * num_
            
    def generate(self):
        window_size = self.window_size
        num_ = self.bundle_size
        is_first_image = True
        data_ = np.zeros((num_,window_size+1,window_size+1), dtype=np.int)
        num_ = num_ - 1
        for f_index in range(self.file_index, self.file_num):
            print 'data generate ready (%d/%d)' %(f_index+1, self.file_num)
            image = readImage(self.file_list[f_index])
#            if self.resize_factor != 0:
#                image = cv2.resize(image, (int(np.round(image.shape[0]*self.resize_factor)),
#                    int(np.round(image.shape[1]*self.resize_factor))), interpolation=cv2.INTER_CUBIC)
            image = DataGenerator.integral(image)
#            print '%d %d' %(image.shape[0],image.shape[1])
            if is_first_image:
                s_i = self.image_index[0]
                s_j = self.image_index[1]
                is_first_image = False
            else:
                s_i = window_size
                s_j = window_size
            for i in range(s_i,image.shape[0]):
                for j in range(s_j,image.shape[1]):
                    data_[num_] = image[i-window_size:i+1,j-window_size:j+1]
                    num_ = num_ - 1
                    if (num_ < 0):
#                        print 'data generate ready (%d/%d)' %(f_index+1, self.file_num)
                        self.file_index = f_index
                        self.image_index = (i, j)
                        return data_
        return None


#generator = DataGenerator(19, ['./train_non_face_scenes/','./negatives/'])

#generator = DataGenerator(19, ['./train_non_face_scenes/'])

#generator = DataGenerator(19, './negatives/', 10000, 0, 3)
#files = generator.getFiles()
#print 'total possible images: %d' %generator.getTotalNum()
#resource = generator.generate()
##generator.printDebugInfo()
##print 'length: %d' %(len(resource))





