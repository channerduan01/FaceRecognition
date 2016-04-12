import numpy as np
from numpy import genfromtxt
from PIL import Image
import matplotlib.pyplot as plt
from numba import double
from numba.decorators import jit, autojit
from timer import Timer
import time


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
                            list_haar.append((i,1,x,y,w,h))
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
#                        if n == best_k-1:
#                            print '%d - %d %d %d %d' %(i,x,y,w,h)
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
def adaboostWeakSelection(w,table_sorted,train_label,N):
    best_k = -1
    best_n = -1
    beat_p = True
    best_err = 1.0
    t_p = np.sum(w[train_label])
    t_n = np.sum(w[~train_label])
    # test code
#    for k in [getHaarFeatureIndex(haar_map, (0, 0, 18, 10, 17, 8))]: 
#    for k in [getHaarFeatureIndex(haar_map, (1, 0, 11, 10, 3, 7))]:     
#    for k in [13533]:
    for k in range(K):
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
#                if 8352 == n:
#                    print 'err_p: %f, err_n: %f, s_n: %f, t_n: %f, s_p: %f, t_p: %f' %(err_p, err_n, s_n, t_n, s_p, t_p)
                best_k = k
                best_n = n
                if err_p < err_n:
                    beat_p = True
                    best_err = err_p
                else:
                    beat_p = False
                    best_err = err_n
    return (best_k,best_n,beat_p,best_err)
adaboostWeakSelection_numba = autojit(adaboostWeakSelection)

def adaboostTrain(deltaT, table_sorted, train_label, adaboost=[], w=[], w_list=[]):
    N = len(train_label)    
    m = np.sum(train_label)
    l = N - m
    if len(w) == 0:
        w = np.zeros((N),np.float)
        w[train_label] = 0.5/m
        w[~train_label] = 0.5/l
        w_list.append(w)
    start_stamp = time.time()
    for i in range(deltaT):
        w_list.append(w.copy())
        w = w/np.sum(w)
        (best_k,best_n,beat_p,best_err) = adaboostWeakSelection_numba(w, table_sorted, train_label, N)
        print 'strong-classifier trained(%d): %.1f%% elapsed:%.0fs' %(i+1, float(i+1)/deltaT*100.0, time.time()-start_stamp)
        if best_n == N-1:
            thre = table[best_k,table_sorted[best_k,best_n]]
        else:
            thre = 0.5 * table[best_k,table_sorted[best_k,best_n]] + 0.5 * table[best_k,table_sorted[best_k,best_n+1]]
            thre = int(np.round(thre))
        print '{\nbest_k = %d,\nbest_n = %d,\nbeat_p = %d,\nbest_err = %f, \nthre = %f\n}' %(best_k,best_n,beat_p,best_err,thre)
        check_res = checkByHaarWeakClassifier(calcuSpecificHaarFeature(train_data,haar_map[best_k]), beat_p, thre)
        check_res1 = table[best_k] <= thre
        print 'c-rate: %.3f, r-t match:%.2f, weighted rate: %.2f' %(float(np.sum(check_res == train_label))/N, 
                                 float(np.sum(check_res == check_res1))/N*100, 1.0-best_err)    
        w_list.append(w.copy())
        w[check_res == train_label] = w[check_res == train_label] * (best_err/(1.0-best_err))
        w_list.append(w.copy())
        adaboost.append((np.log((1.0-best_err)/best_err), thre, best_k, beat_p))
    return adaboost, w, w_list

def checkByAdabost(adaboost, data, label, N):
    check_tmp = np.zeros((N), dtype=np.float)
    a_sum = 0.0
    for (a,thre,best_k,beat_p) in adaboost:
        a_sum = a_sum + a
        check_tmp = check_tmp + a * checkByHaarWeakClassifier(calcuSpecificHaarFeature(data,haar_map[best_k]), beat_p, thre).astype(np.float)
    check_res = check_tmp >= 0.5*a_sum
    print '!strong-classifier(%d) correct rate: %.1f' %(len(adaboost), float(np.sum(check_res == label))/N)

def calcuSpecificHaarFeature(ii, (haar_type, has_trans, x, y, w, h)):
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
        features[i] = calcuSpecificHaarFeature(iis[i], (haar_type, has_trans, x, y, w, h))
    return features
def checkByHaarWeakClassifier(values, p_, thre):
    if p_:
        return values <= thre
    else:
        return values > thre
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
    file_path = 'face_train_channer.csv'  # 'svm.train.normgrey'
    tmp = genfromtxt(open(file_path,'r'), delimiter=' ', dtype='f8', skip_header=2)
    train_data = tmp[:,0:-1]
    window_size = int(np.sqrt(train_data.shape[1]))
    train_data = train_data.reshape((len(train_data),window_size,window_size))
    train_label = tmp[:,-1]>0
    tmp = train_data
    train_data = np.zeros((len(tmp),window_size+1,window_size+1),dtype=np.int)
    for i in range(len(tmp)):
        train_data[i,:,:] = calcuIntegral(np.round(tmp[i,:,:]*255))     
    tmp = None

plt.gray()
#plt.imshow(reverseIntegral(train_data[77,:,:])) # test integral
#----------------------------------------- create basic feature table
if not 'table' in dir():
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
#----------------------------------------- Adaboost train
#adaboost, w = adaboostTrain(3, table_sorted, train_label)
#adaboost, w, w_list = adaboostTrain(5, table_sorted, train_label, adaboost, w)

#adaboost1, w1, w_list = adaboostTrain(2, table_sorted, train_label, adaboost, w)

    
#drawHaarFeatureOnImage(haar_map,best_k)    # test code
#checkByAdabost(adaboost, train_data, train_label, N)



#tt = calcuSpecificHaarFeature(train_data,haar_map[best_k])
#print '%d' %(sum(len(table[best_k]) == tt))

cals = calcuSpecificHaarFeatures(train_data, haar_map[best_k])

#sum_ = 0
#for i in board_p:
#    if i in thre_d:
#        sum_ = sum_ + 1
#print '%d' %sum_
#sum_ = 0
#for i in board_n:
#    if i in thre_d:
#        sum_ = sum_ + 1
#print '%d' %sum_

#list_ = []
#for i_ in np.where(w>0.002)[0]:
#    list_.append('%d (%.1f)' %(i_,w[i_]*100))
#    list_.append(reverseIntegral(train_data[i_,:,:]))
#drawFigures(list_)




