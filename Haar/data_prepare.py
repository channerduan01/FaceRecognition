
import os  
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

face_path = './Caltech_CropFaces/'
non_face_path = './train_non_face_scenes/'

face_files = os.listdir(face_path)[1:]
non_face_files = os.listdir(non_face_path)[1:]


for file_ in non_face_files:
    image = readImage(non_face_path+file_)
    print ' %d, %d ' %(image.shape[0], image.shape[1])







