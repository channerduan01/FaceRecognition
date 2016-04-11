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
def getHaarFeatureMap(list_haars, window_size):
    list_haar = []
    for i in range(len(list_haars)):
        minwidth = list_haars[i][0]
        minheight = list_haars[i][1]
        stepwidth = list_haars[i][2]
        stepheight = list_haars[i][3]
        stridex = list_haars[i][4]
        stridey = list_haars[i][5]
        window_size_l = window_size+1
        for w in range(minwidth,window_size_l,stepwidth):
            for h in range(minheight,window_size_l,stepheight):
                for x in range(w,window_size_l,stridex):
                    for y in range(h,window_size_l,stridey):
                        if i == 0 or i == 1:
                            list_haar.append((i,0,x,y,w,h))
                            list_haar.append((i,1,y,x,h,w))
                        else:
                            list_haar.append((i,0,x,y,w,h))
    return list_haar
def getHaarFeatureIndex(haar_map, haar_setting):
    for i in range(len(haar_map)):
        if haar_map[i] == haar_setting:
            return i
    return -1
# for basis-2
# (x,y) -> (x-w,y-h/2)  +
# (x,y-h/2) -> (x-w,y-h)  -
def calcuBasis2Haar(ii,is_trans,x,y,w,h):
    if not is_trans:
        return calcuRectangle(ii, x, y, w, h/2) - calcuRectangle(ii, x, y-h/2, w, h/2)
    else:
        return calcuRectangle(ii, y, x, h/2, w) - calcuRectangle(ii, y-h/2, x, h/2, w)
# for gap-3
# (x,y) -> (x-w/3,y-h)  +
# (x-w/3,y) -> (x-2*w/3,y-h)  -
# (x-2*w/3,y) -> (x-w,y-h)  +
def calcuGap3Haar(ii,is_trans,x,y,w,h):
    if not is_trans:
        return calcuRectangle(ii, x, y, w/3, h) + calcuRectangle(ii, x-2*w/3, y, w/3, h) - 2*calcuRectangle(ii, x-w/3, y, w/3, h)
    else:
        return calcuRectangle(ii, y, x, h, w/3) + calcuRectangle(ii, y, x-2*w/3, h, w/3) - 2*calcuRectangle(ii, y, x-w/3, h, w/3)
# for square-3    
# (x,y) -> (x-w/2,y-h/2) +
# (x-w/2,y-h/2) -> (x-w,y-h) +
# (x-w/2,y) -> (x-w,y-h/2) -
# (x,y-h/2) -> (x-w/2,y-h) -    
def calcuSquare4Haar(ii,x,y,w,h):
    return calcuRectangle(ii, x, y, w/2, h/2) + calcuRectangle(ii, x-w/2, y-h/2, w/2, h/2) \
        - calcuRectangle(ii, x-w/2, y, w/2, h/2) + calcuRectangle(ii, x, y-h/2, w/2, h/2)
# specific functions, boosting the performance of Adaboost training
# it copys the process of calcuBasis2Haar calcuGap3Haar calcuSquare4Haar to speed up whole process
def calcuRectangle(ii, x_end, y_end, w, h):
    return ii[y_end,x_end] + ii[y_end-h,x_end-w] - ii[y_end,x_end-w] - ii[y_end-h,x_end]
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
def reverseIntegral(ii):
    image = np.zeros((ii.shape[0]-1,ii.shape[1]-1),dtype=np.int)
    for x in range(1, ii.shape[0]):
        for y in range(1, ii.shape[1]):
            image[x-1,y-1] = calcuRectangle(ii,y,x,1,1)
    return image

    
# Core of Adaptive Boost, select the best weak classifier
# w is the weight for training data now
# m indicates the postive or negtive of training data
def adaboostWeakSelection(w,m,table_sorted,train_label):
    best_k = -1
    best_n = -1
    beat_p = True
    best_err = 1.0
    t_p = np.sum(w[:m])
    t_n = np.sum(w[m:])
    # test code
#    for k in [getHaarFeatureIndex(haar_map, (0, 0, 18, 10, 17, 8))]: 
    for k in [getHaarFeatureIndex(haar_map, (1, 0, 11, 10, 3, 7))]:     
#    for k in range(K):
        tmp = table_sorted[k,:]
        s_p = 0
        s_n = 0
        for n in range(N):
            if train_label[tmp[n]]:
                s_p = s_p + w[tmp[n]]
            else:
                s_n = s_n + w[tmp[n]]
            err_p = s_n + t_p - s_p
            err_n = s_p + t_n - s_n
            if err_p < best_err or err_n < best_err:
                best_k = k
                best_n = n
                if err_p < err_n:
                    beat_p = True
                    best_err = err_p
                else:
                    print '%.8f' %err_n
                    beat_p = False
                    best_err = err_n    
    return (best_k,best_n,beat_p,best_err)
adaboostWeakSelection_numba = autojit(adaboostWeakSelection)
def checkImgByHaarWeakClassifier(ii, (haar_type, has_trans, x, y, w, h), p_, thre):
    value = 0
    if haar_type == 0:
        value = calcuBasis2Haar(ii, has_trans, x, y, w, h)
    elif haar_type == 1:
        value = calcuGap3Haar(ii, has_trans, x, y, w, h)
    else:
        value = calcuSquare4Haar(ii, x, y, w, h)
    if p_:
        return value <= thre
    else:
        return value > thre
def drawHaarFeatureOnImage(haar_map,k):
    (haar_type, has_trans, x, y, w, h) = haar_map[k]
    # raw image start from 0, integral start from 1, that is different
    x = x-1
    y = y-1
    image = reverseIntegral(train_data[80,:,:])
    plt.figure()
    plt.imshow(image) 
    image = np.round(image.astype(np.double)/np.max(image)*128) # turn down the original image
    image[x-w+1:x+1,y-h+1:y+1] = 255
    plt.figure()
    plt.imshow(image) 

if not 'train_data' in dir() or not 'train_label' in dir():
    tmp = genfromtxt(open('svm.train.normgrey','r'), delimiter=' ', dtype='f8', skip_header=2)
    train_data = tmp[:,0:-1]
    data_size = int(np.sqrt(train_data.shape[1]))
    train_data = train_data.reshape((len(train_data),data_size,data_size))
    train_label = tmp[:,-1]>0
    tmp = train_data
    train_data = np.zeros((len(tmp),data_size+1,data_size+1),dtype=np.int)
    for i in range(len(tmp)):
        train_data[i,:,:] = calcuIntegral(np.round(tmp[i,:,:]*255))     
    tmp = None

plt.gray()
#plt.imshow(reverseIntegral(train_data[77,:,:])) # test integral
#----------------------------------------- create basic feature table
if not 'table' in dir():
    window_size = data_size
    haars = [(1,2,1,2,1,1),(3,1,3,1,1,1),(2,2,2,2,1,1)]
    haar_map = getHaarFeatureMap(haars,window_size)   # The map to original haar setting
    K = len(haar_map)
    N = len(train_data)
    with Timer() as t:
        table = np.zeros((K,N),dtype=np.int)
        for i in range(N):
            calcuHaarFeature_numba(table[:,i],train_data[i,:,:],haars,window_size)
    print 'create table: %.2fs' % t.secs
#----------------------------------------- create sorted table    
if not 'table_sorted' in dir():    
    table_sorted = np.zeros_like(table)
    with Timer() as t:
        for i in range(K):
            table_sorted[i,:] = np.argsort(table[i,:])
    print 'create table_sorted: %.2fs' % t.secs
#----------------------------------------- Adaboost
# T indicates the feature number within this Adaboost
T = 1
m = np.sum(train_label)   # m can be the threshold of postive or negative data
l = N - m
w = np.zeros((N),np.float)
w[:m] = 0.5/m
w[m:] = 0.5/l
# Simple Test
with Timer() as t:
    (best_k,best_n,beat_p,best_err) = adaboostWeakSelection_numba(w,m,table_sorted,train_label)
print 'select weak classifier: %.2fs' % t.secs
drawHaarFeatureOnImage(haar_map,best_k)
if best_n == N-1:
    thre = 1.0 * table[best_k,table_sorted[best_k,best_n]]
else:
    thre = 0.5 * table[best_k,table_sorted[best_k,best_n]] + 0.5 * table[best_k,table_sorted[best_k,best_n+1]]
#check_ = np.zeros((N), dtype=np.bool)
#for i in range(N):
#    check_[i] = checkImgByHaarWeakClassifier(train_data[i,:,:],haar_map[best_k], beat_p, thre)
#print 'train correct rate: %.2f (expected rate: %.2f)' %(float(np.sum(check_ == train_label))/N, 1.0-best_err)

#for i in range(T):
#    1==1

#test_best_k = getHaarFeatureIndex(haar_map,(0,0,17,8,18,10))









