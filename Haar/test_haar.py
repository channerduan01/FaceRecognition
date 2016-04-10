

from PIL import Image
import numpy as np
import matplotlib.pyplot as plt
from numba import double
from numba.decorators import jit, autojit
from timer import Timer

def readImage(path):
    '''Read image as np array, from path'''
    im = np.array(Image.open(path).convert('L'))
    return im


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
    

def calcuHaarFeature(ii, list_haars, window_size):
    list_ = []
    window_size_l = window_size+1
    for i in range(len(list_haars)):
#        haar  = list_haars[i]
#        minwidth = haar.minwidth
#        minheight = haar.minheight
#        stepwidth = haar.stepwidth
#        stepheight = haar.stepheight
#        stridex = haar.stridex
#        stridey = haar.stridey
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
                        # coordinates start from 1 to window_size
#                        haar.estimate(list_,ii, x, y, w, h)
                        if i == 0:
                            list_.append(calcuRectangle(ii, x, y, w, h/2) - calcuRectangle(ii, x, y-h/2, w, h/2))
                            list_.append(calcuRectangle(ii, y, x, h/2, w) - calcuRectangle(ii, y-h/2, x, h/2, w))
                        elif i == 1:
                            list_.append(calcuRectangle(ii, x, y, w/3, h) + calcuRectangle(ii, x-2*w/3, y, w/3, h) - 2*calcuRectangle(ii, x-w/3, y, w/3, h))
                            list_.append(calcuRectangle(ii, y, x, h, w/3) + calcuRectangle(ii, y, x-2*w/3, h, w/3) - 2*calcuRectangle(ii, y, x-w/3, h, w/3))
                        else:
                            list_.append(calcuRectangle(ii, x, y, w/2, h/2) + calcuRectangle(ii, x-w/2, y-h/2, w/2, h/2) \
                                - calcuRectangle(ii, x-w/2, y, w/2, h/2) + calcuRectangle(ii, x, y-h/2, w/2, h/2)
    )
    return list_
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
    
# test for integral image interface    
def testIntegral():
    image = readImage('simple_test.png')
    plt.gray()
    plt.imshow(image)
    image1 = np.zeros_like(image)
    ii = calcuIntegral(image)
    for x in range(1, ii.shape[0]):
        for y in range(1, ii.shape[1]):
            image1[x-1,y-1] = calcuRectangle(ii,x,y,1,1)
    plt.figure()
    plt.imshow(image1)
    
    
    
##---------------------------------------------------------------------------# too slow!    
    
# for basis-2
# (x,y) -> (x-w,y-h/2)  +
# (x,y-h/2) -> (x-w,y-h)  -
#def calcuBasis2Haar(list_,ii,x,y,w,h):
#    list_.append(calcuRectangle(ii, x, y, w, h/2) - calcuRectangle(ii, x, y-h/2, w, h/2))
#    list_.append(calcuRectangle(ii, y, x, h/2, w) - calcuRectangle(ii, y-h/2, x, h/2, w))
# for gap-3
# (x,y) -> (x-w/3,y-h)  +
# (x-w/3,y) -> (x-2*w/3,y-h)  -
# (x-2*w/3,y) -> (x-w,y-h)  +
#def calcuGap3Haar(list_,ii,x,y,w,h):
#    list_.append(calcuRectangle(ii, x, y, w/3, h) + calcuRectangle(ii, x-2*w/3, y, w/3, h) - 2*calcuRectangle(ii, x-w/3, y, w/3, h))
#    list_.append(calcuRectangle(ii, y, x, h, w/3) + calcuRectangle(ii, y, x-2*w/3, h, w/3) - 2*calcuRectangle(ii, y, x-w/3, h, w/3))
# for square-3    
# (x,y) -> (x-w/2,y-h/2) +
# (x-w/2,y-h/2) -> (x-w,y-h) +
# (x-w/2,y) -> (x-w,y-h/2) -
# (x,y-h/2) -> (x-w/2,y-h) -    
#def calcuSquare4Haar(list_,ii,x,y,w,h):
#    list_.append(calcuRectangle(ii, x, y, w/2, h/2) + calcuRectangle(ii, x-w/2, y-h/2, w/2, h/2) \
#        - calcuRectangle(ii, x-w/2, y, w/2, h/2) + calcuRectangle(ii, x, y-h/2, w/2, h/2)
#    )
#class haar_feature:
#    def __init__(self, name, minwidth, minheight, stepwidth, stepheight, stridex, stridey, estimate_fun):
#        self.name = name
#        self.minwidth = minwidth
#        self.minheight = minheight
#        self.stepwidth = stepwidth
#        self.stepheight = stepheight
#        self.stridex = stridex
#        self.stridey = stridey
#        self.estimate = estimate_fun
##---------------------------------------------------------------------------# too slow!

image = readImage('simple_test.png')
plt.gray()
#plt.imshow(image)

ii = calcuIntegral(image)
#ii = train_data[0,:,:]

window_size = 19
print 'basis-2: %d' %(getHaarFeatureNum((1,2,1,2,1,1),window_size)*2)
print 'gap-3: %d' %(getHaarFeatureNum((3,1,3,1,1,1),window_size)*2)
print 'square-4: %d' %getHaarFeatureNum((2,2,2,2,1,1),window_size)



#list_haars = []
#list_haars.append(haar_feature('basis_2',1,2,1,2,1,1,calcuBasis2Haar))
#list_haars.append(haar_feature('gap-3',3,1,3,1,1,1,calcuGap3Haar))
#list_haars.append(haar_feature('square-4',2,2,2,2,1,1,calcuSquare4Haar))
#with Timer() as t:
#    for i in range(10000):
#        list_ = calcuHaarFeature_numba(ii,[(1,2,1,2,1,1),(3,1,3,1,1,1),(2,2,2,2,1,1)],window_size)
#print '%.2fs' % t.secs
#print 'all: %d' %len(list_)





