import numpy as np
from numpy import genfromtxt
from PIL import Image
import matplotlib.pyplot as plt
from numba import double
from numba.decorators import jit, autojit
from timer import Timer

def getHaarFeatureNum((minwidth, minheight, stepwidth, stepheight, stridex, stridey), window_size):
    num = 0
    window_size_l = window_size+1
    for w in range(minwidth,window_size_l,stepwidth):
        for h in range(minheight,window_size_l,stepheight):
            for x in range(w,window_size_l,stridex):
                for y in range(h,window_size_l,stridey):
                    num = num+1
    return num
def calcuRectangle(ii, x_end, y_end, w, h):
    return ii[x_end,y_end] + ii[x_end-w,y_end-h] - ii[x_end-w,y_end] - ii[x_end,y_end-h]
calcuRectangle = autojit(calcuRectangle)
def calcuHaarFeature(table, ii, list_haars, window_size):
    n = 0
    window_size_l = window_size+1
    for i in range(len(list_haars)):
        minwidth = list_haars[i][0]
        minheight = list_haars[i][1]
        stepwidth = list_haars[i][2]
        stepheight = list_haars[i][3]
        stridex = list_haars[i][4]
        stridey = list_haars[i][5]
        for w in range(minwidth,window_size_l,stepwidth):
            for h in range(minheight,window_size_l,stepheight):
                for x in range(w,window_size_l,stridex):
                    for y in range(h,window_size_l,stridey):
                        # (x-w+1,y-h+1) -> (x,y)
                        if i == 0:
                            table[n] = calcuRectangle(ii, x, y, w, h/2) - calcuRectangle(ii, x, y-h/2, w, h/2)
                            table[n+1] = calcuRectangle(ii, y, x, h/2, w) - calcuRectangle(ii, y-h/2, x, h/2, w)
                            n = n+2
                        elif i == 1:
                            table[n] = calcuRectangle(ii, x, y, w/3, h) + calcuRectangle(ii, x-2*w/3, y, w/3, h) - 2*calcuRectangle(ii, x-w/3, y, w/3, h)
                            table[n+1] = calcuRectangle(ii, y, x, h, w/3) + calcuRectangle(ii, y, x-2*w/3, h, w/3) - 2*calcuRectangle(ii, y, x-w/3, h, w/3)
                            n = n+2
                        else:
                            table[n] = calcuRectangle(ii, x, y, w/2, h/2) + calcuRectangle(ii, x-w/2, y-h/2, w/2, h/2) \
                                - calcuRectangle(ii, x-w/2, y, w/2, h/2) + calcuRectangle(ii, x, y-h/2, w/2, h/2)
                            n = n+1
    return n
calcuHaarFeature_numba = autojit(calcuHaarFeature)
    
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
def drawIntegral(ii):
    image = np.zeros((ii.shape[0]-1,ii.shape[1]-1),dtype=np.int)
    for x in range(1, ii.shape[0]):
        for y in range(1, ii.shape[1]):
            image[x-1,y-1] = calcuRectangle(ii,x,y,1,1)
    plt.figure()
    plt.imshow(image)
    

if not 'train_data' in dir() or not 'train_label' in dir():
    tmp = genfromtxt(open('svm.train.normgrey','r'), delimiter=' ', dtype='f8', skip_header=2)
    train_data = tmp[:,0:-1]
    data_size = int(np.sqrt(train_data.shape[1]))
    train_data = train_data.reshape((len(train_data),data_size,data_size))
    train_label = tmp[:,-1].astype(np.int)
    
    tmp = train_data
    train_data = np.zeros((len(tmp),data_size+1,data_size+1),dtype=np.int)
    for i in range(len(tmp)):
        train_data[i,:,:] = calcuIntegral(np.round(tmp[i,:,:]*255))       
 
plt.gray()
#drawIntegral(train_data[77,:,:])

window_size = data_size
haars = [(1,2,1,2,1,1),(3,1,3,1,1,1),(2,2,2,2,1,1)]
K = getHaarFeatureNum(haars[0],window_size)*2 + getHaarFeatureNum(haars[1],window_size)*2 + getHaarFeatureNum(haars[2],window_size)
N = len(train_data)

with Timer() as t:
    table = np.zeros((K,N),dtype=np.int)
    for i in range(N):
        calcuHaarFeature_numba(table[:,i],train_data[i,:,:],haars,window_size)
print '%.2fs' % t.secs 









