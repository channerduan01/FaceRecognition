import numpy as np
from numpy import genfromtxt
import matplotlib.pyplot as plt
from numba.decorators import jit, autojit
from timer import Timer
from data_generator import DataGenerator
import time

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
        is_trans = list_haars[i][6]
        window_size_l = window_size+1
        for w in range(minwidth,window_size_l,stepwidth):
            for h in range(minheight,window_size_l,stepheight):
                for x in range(w,window_size_l,stridex):
                    for y in range(h,window_size_l,stridey):
                        if is_trans:
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
def adaboostWeakSelection(w, mytable_sorted, label, N):
    best_k = -1
    best_n = -1
    beat_p = True
    best_err = 1.0
    t_p = np.sum(w[label])
    t_n = np.sum(w[~label])
    # test code
#    for k in [getHaarFeatureIndex(haar_map, (0, 0, 18, 10, 17, 8))]: 
#    for k in [getHaarFeatureIndex(haar_map, (1, 0, 11, 10, 3, 7))]:     
#    for k in [13533]:
    for k in range(K):
        tmp = mytable_sorted[k,:]
        s_p = 0
        s_n = 0
        for n in range(N):
            if label[tmp[n]]:
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
# main training process for Adaptive Boost
def adaboostTrain(deltaT, mytable, mytable_sorted, data, label, adaboost=None, w=None, is_debug=False):
    N = len(label)    
    m = np.sum(label)
    l = N - m
    if adaboost is None:
    # if I insert this [] as default value of 'adaboost', adaboostTrain can only create one []! 
    # made weird bug! bad design of python for memoery allocation, 
    # beware of default value of parameter, it should not be the value that you want to return!
        adaboost = []
    if w is None:
        w = np.zeros((N),np.float)
        w[label] = 0.5/m
        w[~label] = 0.5/l
    start_stamp = time.time()
    for i in range(deltaT):
        w = w/np.sum(w)
        (best_k,best_n,beat_p,best_err) = adaboostWeakSelection_numba(w, mytable_sorted, label, N)
        print 'adaboost: %.1f%% (%d) elapsed:%.0fs' %(float(i+1)/deltaT*100.0, len(adaboost)+1, time.time()-start_stamp)
        if best_n == N-1:
            thre = mytable[best_k,mytable_sorted[best_k,best_n]]
        else:
            thre = 0.5 * mytable[best_k,mytable_sorted[best_k,best_n]] + 0.5 * mytable[best_k,mytable_sorted[best_k,best_n+1]]
            thre = int(np.round(thre))
        if is_debug:
            print '{\nbest_k = %d\nbest_n = %d\nbeat_p = %d\nthre = %f\nbest_err = %f\n}' %(best_k,best_n,beat_p,best_err,thre)
        check_res = checkByHaarWeakClassifier(calcuSpecificHaarFeatures(data,haar_map[best_k]), beat_p, thre)
        if is_debug:
            if beat_p:      
                check_res1 = mytable[best_k] <= thre
            else:
                check_res1 = mytable[best_k] > thre
            print '   c-rate: %.3f\n   r-t match:%.1f%%\n   weighted rate: %.2f' \
                %(float(np.sum(check_res == label))/N, \
                  float(np.sum(check_res == check_res1))/N*100, 1.0-best_err) 
        w[check_res == label] = w[check_res == label] * (best_err/(1.0-best_err))
        adaboost.append((np.log((1.0-best_err)/best_err), thre, best_k, beat_p))
        if is_debug:
            print '   adaboost-rate: %.6f%%' \
                %(float(np.sum(checkByAdaboost(adaboost, data, N) == label))/N*100)       
    return adaboost, w

def checkByAdaboost(adaboost, data, N, fade_param=1.0):
    check_tmp = np.zeros((N), dtype=np.float)
    a_sum = 0.0
    for (a,thre,best_k,beat_p) in adaboost:
        a_sum = a_sum + a
        check_tmp = check_tmp + a * checkByHaarWeakClassifier(calcuSpecificHaarFeatures(data,haar_map[best_k]), beat_p, thre).astype(np.float)
    return check_tmp >= 0.5*a_sum*fade_param
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
fastCheckByCascade = autojit(fastCheckByCascade)
def checkByCascade(cascade, data):
    N = len(data)
    check_tmp = np.zeros((N), dtype=np.bool)
    index_ = np.asarray(range(N))
    data_ = data
    for i in range(len(cascade)):
        adaboost, fade_param = cascade[i]
        check_res = checkByAdaboost(adaboost, data_, N, fade_param)
        index_tmp = np.where(check_res)[0]
        N = len(index_tmp)
        data_ = data_[index_tmp]
        index_ = index_[index_tmp]
#        print 'cascade layer %d, N: %d' %(i, N)
    check_tmp[index_] = True
    return check_tmp
checkByCascade = autojit(checkByCascade)
def drawHaarFeatureOnImage(img,haar_map,k):
    (haar_type, has_trans, x, y, w, h) = haar_map[k]
    # raw image start from 0, integral start from 1, that is different
    x = x-1
    y = y-1
    image = reverseIntegral(img)
    plt.figure()
    plt.imshow(image) 
    image = np.round(image.astype(np.double)/np.max(image)*128) # turn down the original image
    image[x-w+1:x+1,y-h+1:y+1] = 255
    plt.figure()
    plt.imshow(image) 

def loaddata(file_path):
    tmp = genfromtxt(open(file_path,'r'), delimiter=' ', dtype='f8', skip_header=2)
    data = tmp[:,0:-1]
    window_size = int(np.sqrt(data.shape[1]))
    data = data.reshape((len(data),window_size,window_size))
    label = tmp[:,-1]>0
    tmp = data
    data = np.zeros((len(tmp),window_size+1,window_size+1),dtype=np.int)
    for i in range(len(tmp)):
        data[i,:,:] = calcuIntegral(np.round(tmp[i,:,:]*255))     
    tmp = None
    return (data, label, window_size)
if not 'train_data' in dir() or not 'train_label' in dir():
    ## small data set for face
#    (train, train_label, window_size) = loaddata('svm.train.normgrey')  
#    (test, test_label, window_size) = loaddata('svm.test.normgrey')  # small data set

##   my own, larger data set
   (test_data, test_label, window_size) = loaddata('face_test.csv')
   (train_positive, tmp, window_size) = loaddata('face_train_positive.csv')
   (train_negtive, train_negtive_label, window_size) = loaddata('face_train_negtive.csv')
   tmp = None
   mark_f = train_positive.shape[0]
   mark_n = 4000
   train_data = np.zeros((mark_f+mark_n,train_positive.shape[1],train_positive.shape[2]), np.int64)
   train_label = np.zeros((mark_f+mark_n), np.bool)
   train_data[:mark_f] = train_positive
   train_data[mark_f:] = train_negtive[:mark_n]
   train_label[:mark_f] = True
   train_label[mark_f:] = False

plt.gray()
haars = [(2,4,1,2,1,1,1),(3,2,3,1,1,1,1),(2,2,2,2,1,1,0)]
#plt.imshow(reverseIntegral(train_data[77,:,:])) # test integral
#---------------------------------------------------------- create basic feature table
if not 'base_table' in dir():
    # map to original haar setting
    haar_map = getHaarFeatureMap(haars,window_size)
    K = len(haar_map)
    N = len(train_data)
    with Timer() as t:
        base_table = np.zeros((K,N),dtype=np.int)
        for i in range(N):
            calcuHaarFeature_numba(base_table[:,i],train_data[i,:,:],haars,window_size)
    print 'create table: %.2fs' % t.secs
#---------------------------------------------------------- create sorted table
if not 'base_table_sorted' in dir():
    base_table_sorted = np.zeros_like(base_table)
    with Timer() as t:
        for i in range(K):
            base_table_sorted[i,:] = np.argsort(base_table[i,:])
    print 'create table_sorted: %.2fs' % t.secs
#---------------------------------------------------------- Adaboost train
def drawOutlierOfData(w, threshold, data):
    list_ = []
    for i_ in np.where(w > threshold)[0]:
        list_.append('(%.1f)' %(w[i_]*100))
        list_.append(reverseIntegral(data[i_,:,:]))
    drawFigures(list_)
def adaboostTesting(adaboost, data, label):
    N = len(label)
    for i in range(len(adaboost)):
        print '%d rate: %.6f%%' %(i, float(np.sum(checkByAdaboost(adaboost[:i+1], data, N) == label))/N*100)  
def saveAdaboost(filename, haars, adaboost, w):
    np.save(filename,(haars, adaboost, w))
def loadAdaboost(filename):
    (haars, adaboost, w) = np.load(filename)
    return (haars, adaboost, w)
filename = None
#filename = ''
#filename = 'ada_small_face_test.npy'
#filename = 'ada_my_own_data_face_test.npy'
if not filename is None:
    if filename == '':
        # Simple train
        adaboost, w = adaboostTrain(20, base_table, base_table_sorted, train_data, train_label, is_debug=True)
#        adaboost, w = adaboostTrain(12, table_tmp, table_sorted_tmp, train_data_tmp, train_label_tmp, is_debug=True)
    else:
        # Load and train
        (haars, adaboost, w) = loadAdaboost(filename)
        adaboost, w = adaboostTrain(129, base_table, base_table_sorted, train_data, train_label, adaboost, w)
        saveAdaboost(filename, haars, adaboost, w)
## test code
#else:
#    drawHaarFeatureOnImage(train_data[80,:,:],haar_map,36830)
#    drawOutlierOfData(w_list[-1], 0.005, train_data)
#    adaboostTesting(adaboost, train_data, train_label)   # remember train the adaboost for this test 
#    adaboostTesting(adaboost, test_data, test_label)

#---------------------------------------------------------- Detection
#image = calcuIntegral(readImage('my_profile.jpg'))
#
#generator = DataGenerator(window_size, './negatives/', 100000)
#
##def getFalsePostiveData(cascade, generate_num):
##    list_res = []
##    while generate_num > len(list_res):
##        data_tmp = generator.generate()
##        check_res = checkByCascade(cascade, data_tmp)
##        list_res.extend(data_tmp[check_res].tolist())
##    return list_res
#
#with Timer() as t:
##    (num_, select_) = getFalsePostiveData(cascade_, 100)
##    list_res = getFalsePostiveData(cascade_, 4000)
##    data_tmp = generator.generate()
##    check_res = checkByCascade(cascade_, data_tmp)
#    
##print 'total: %d, select: %d, time-cost: %.2fs' %(1, len(list_res), t.secs)
#
#num_ = 0
#select_ = 0
#with Timer() as t:
#    for i in range(window_size,image.shape[0]):
#        for j in range(window_size,image.shape[1]):
#            num_ = num_ + 1
##            if fastCheckByCascade(cascade_, image[i-window_size:i+1,j-window_size:j+1]):
##                select_ = select_ + 1
#print 'total: %d, select: %d, time-cost: %.2fs' %(num_, select_, t.secs)

#---------------------------------------------------------- Cascade train
def saveCascade(filename, cascade):
    np.save(filename, cascade)
def loadCascade(filename):
    return np.load(filename).tolist()
def evalueDandF(cascade, data, label):
    check_res = checkByCascade(cascade, data)
    d = float(np.sum(check_res[label]))/np.sum(label)
    f = float(np.sum(~label[check_res]))/len(check_res)
    return (d, f)
# Key image generator
generator = DataGenerator(window_size, ['./train_non_face_scenes/','./negatives/'], 10000, 0, 1.0)
file_index_ = 0
def getFalsePostiveData(cascade, generate_num):
    global file_index_
    list_res = []
    while generate_num > len(list_res):
        data_tmp = generator.generate()
        if data_tmp is None: break
        check_res = checkByCascade(cascade, data_tmp)
        list_res.extend(data_tmp[check_res].tolist())
    file_index_ = generator.getDebugInfo()
    return list_res
def prepareData(cascade):
    if len(cascade) == 0:
        return (train_data, train_label, base_table, base_table_sorted)
    else:
        start_stamp = time.time()
        if len(cascade) == 1:
            print 'warmup for %d layer' %len(cascade)
            fp_data = train_negtive[checkByCascade(cascade_, train_negtive)][:mark_n]
        else:
            print 'preparing data for %d layer' %len(cascade)
            fp_data = np.asarray(getFalsePostiveData(cascade_, mark_n)[:mark_n])
        
        neg_num = len(fp_data)
        pos_num = mark_f
        if pos_num > neg_num: pos_num = neg_num
        train_data_tmp = np.zeros((pos_num+neg_num,train_positive.shape[1],train_positive.shape[2]), np.int64)
        train_data_tmp[:pos_num] = train_data[:pos_num]
        train_data_tmp[pos_num:] = fp_data
        train_label_tmp = np.zeros((pos_num+neg_num), np.bool)
        train_label_tmp[:pos_num] = True
        train_label_tmp[pos_num:] = False
        print '    data loaded, size: %d , elapsed:%.0fs' %(pos_num+neg_num, time.time()-start_stamp)
        table_tmp = np.zeros((K,pos_num+neg_num),dtype=np.int)
        table_tmp[:,:pos_num] = base_table[:,:pos_num]
        for i in range(neg_num):
            calcuHaarFeature_numba(table_tmp[:,pos_num+i],train_data_tmp[pos_num+i,:,:],haars,window_size)
        print '    table ready, elapsed:%.0fs' %(time.time()-start_stamp)
        table_sorted_tmp = np.zeros_like(table_tmp)
        for i in range(K):
            table_sorted_tmp[i,:] = np.argsort(table_tmp[i,:])
        print '    table sorted, prepare finish, elapsed:%.0fs' %(time.time()-start_stamp)
        return (train_data_tmp, train_label_tmp, table_tmp, table_sorted_tmp)
    
## test code, test 'checkByAdaboost'
#cascade_ = []
#adaboost, w = adaboostTrain(3, table_sorted, train_label)
#cascade_.append((adaboost, 1.0))
#cascade_.append((adaboost, 0.75))
#check_res = checkByCascade(cascade_, train_data)
#check_res1 = checkByAdaboost(adaboost, test_data, len(test_label), 1.0)
#d = float(np.sum(check_res1[test_label]))/np.sum(test_label)
#f = float(np.sum(~test_label[check_res1]))/len(check_res1)
#print 'd: %f, f: %f' %(d, f)
#print '%f' %(float(np.sum(check_res==check_res1))/N*100)
## test code, test 'checkByAdaboost'
#a1,w = adaboostTrain(1, table_sorted, train_label)
#a2,w = adaboostTrain(2, table_sorted, train_label)
#cascade_.append((a1, 0.75))
#cascade_.append((a1, 0.75))
#check_res = checkByCascade(cascade_, valid_data)


#---------------------------------- Core of Cascade
valid_data = test_data
valid_label = test_label
#valid_data = train_data
#valid_label = train_label

filename = None
#filename = ''
#filename = 'cascade_my_own_data_face_test.npy'

max_layer_num = 6
F_target = 0.01
constaints_list = [(0.99,0.5),(0.98,0.7),(0.98,0.7),(0.98,0.7),(0.99,0.7), \
                   (0.99,0.7),(0.99,0.7),(0.99,0.7),(0.99,0.7),(0.99,0.7), \
                   ]
if not filename is None:
    if filename == '':
        cascade_ = []
        D_all = 1.0
        F_all = 1.0
    else:
        cascade_ = loadCascade(filename)
        (D_all, F_all) = evalueDandF(cascade_, valid_data, valid_label)
    i = len(cascade_)
    train_data_tmp = None
    train_label_tmp = None
    while F_all > F_target and i < len(constaints_list) and i < max_layer_num:
        (train_data_tmp, train_label_tmp, table_tmp, table_sorted_tmp) = prepareData(cascade_)
        (d_min, f_max) = constaints_list[i]
        D_all = D_all * d_min
        F_all = F_all * f_max
        D_ = 0.0
        F_ = 1.0
        adaboost_ = None
        w_ = None
        t = 0
        while not (D_ > D_all and F_ < F_all):
            t = t + 1
            print 'layer %d train Adaboost size:%d' %(i,t)
            adaboost_, w_ = adaboostTrain(1, table_tmp, table_sorted_tmp, train_data_tmp, train_label_tmp, adaboost_, w_)
            fade_param = 1.0
            delta = 0.05
            if t == 1: cascade_.append((adaboost_, fade_param))
            print '    start tuning->\n    D_all: %f, F_all: %f' %(D_all,F_all)
            for tune_ in range(19):
                cascade_[-1] = (cascade_[-1][0], fade_param)
                (D_, F_) = evalueDandF(cascade_, valid_data, valid_label)
                print '    %f, %f' %(D_,F_)
                if F_ > F_all: break
                if D_ > D_all and F_ < F_all: break
                fade_param = fade_param - delta
        D_all = D_
        F_all = F_
        if filename != '':
            saveCascade(filename, cascade_)
        i = i + 1
    (D_all, F_all) = evalueDandF(cascade_, valid_data, valid_label)
    print '\nFinal-size %d,  D_all: %f, F_all: %f' %(len(cascade_),D_all,F_all)














