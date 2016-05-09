import os.path
import cv2
import numpy as np
import shutil

BASE_PATH="/Users/channerduan/Documents/study/Face_Recog/Data/Android_ear_cropped_test"
OUTPUT_PATH="/Users/channerduan/Documents/study/Face_Recog/Data/Android_ear_cropped_test/arranged"
SEPARATOR=";"


def makedir(path):
    if (os.path.exists(path) == False):
        os.mkdir(path)

makedir(OUTPUT_PATH)
THRESHOULD = 10 # key point!!!
FORM_WIDTH = 47
FORM_HEIGHT = 78


for directory in os.listdir(BASE_PATH):
    if os.path.isdir(BASE_PATH+'/'+directory):
        print directory
        new_directory = '%s%s/' %(OUTPUT_PATH+"/", directory)
        makedir(new_directory)
        new_index = 0
        for filename in os.listdir(BASE_PATH+'/'+directory):
            if filename.find('.jpg') == -1:
                continue
            print "    %s" %filename
            image = cv2.cvtColor(cv2.imread(BASE_PATH+'/'+directory+'/'+filename),cv2.COLOR_RGB2GRAY)
            image = cv2.resize(image,(FORM_WIDTH,FORM_HEIGHT),interpolation=cv2.INTER_CUBIC)
            new_index = new_index+1
            new_file_path = '%s/%d%s' %(new_directory,new_index,'.jpg')
            cv2.imwrite(new_file_path, image)
            if new_index == THRESHOULD:
                break

















