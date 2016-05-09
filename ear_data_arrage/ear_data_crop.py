import os.path
import cv2
import numpy as np
import matplotlib.pyplot as plt


BASE_PATH="/Users/channerduan/Documents/study/Face_Recog/DatabaseEars_Southampton/DatabaseEars/"
OUTPUT_PATH="/Users/channerduan/Documents/study/Face_Recog/Data/Ear_cropped_by_myself"
SEPARATOR=";"

left_ear_cascade = cv2.CascadeClassifier('cascade_files/haarcascade_mcs_leftear_.xml')
right_ear_cascade = cv2.CascadeClassifier('cascade_files/haarcascade_mcs_rightear_.xml')

if left_ear_cascade.empty():
	raise IOError('Unable to load the left ear cascade classifier xml file')

if right_ear_cascade.empty():
	raise IOError('Unable to load the right ear cascade classifier xml file')

def makedir(path):
    if (os.path.exists(path) == False):
        os.mkdir(path)

def readImage(filepath):
    return cv2.cvtColor(cv2.imread(filepath), cv2.COLOR_BGR2GRAY)
    
def rotate_image(img, ang):
    rows,cols = img.shape
    M = cv2.getRotationMatrix2D((cols/2,rows/2),ang,1)
    dst = cv2.warpAffine(img,M,(cols,rows))
    return dst

plt.gray()
makedir(OUTPUT_PATH)
num_pic = 0
num_detected = 0
list_valid_person_id = []


angle_range = [0,1,-1,2,-2,3,-3,4,-4,5,-5]
tmp_count_num = 0
for filename in os.listdir(BASE_PATH):
    if filename.find('.jpg') == -1:
       continue
    num_pic = num_pic+1
    person_id = int(filename.split('_')[0])   
    if len(list_valid_person_id) == 0 or list_valid_person_id[-1] != person_id:
        list_valid_person_id.append(person_id)
        tmp_count_num = 0
    tmp_count_num = tmp_count_num+1
    tmp_cropped_id = 0
    gray = readImage(BASE_PATH+filename)
    for angle in angle_range:
        if angle != 0:
            tmp_gray = rotate_image(gray, angle)
        else:
            tmp_gray = gray
        left_ear = left_ear_cascade.detectMultiScale(tmp_gray, 1.05, 5)
        if (len(left_ear) == 1):
            num_detected = num_detected+1
            tmp_cropped_id = tmp_cropped_id+1
            outputFile = '%s/%d-%d-%d-.jpg' %(OUTPUT_PATH, person_id, tmp_count_num, tmp_cropped_id)
            (x,y,w,h) = left_ear[0]
#            cv2.rectangle(tmp_gray, (x,y), (x+w,y+h), (255,0,0), 3)
#            plt.figure()
#            plt.imshow(tmp_gray)
            cv2.imwrite(outputFile, tmp_gray[y:y+h,x:x+w])
#    break
        
    print '%s/%d-%d' %(BASE_PATH+filename, len(list_valid_person_id), tmp_count_num)
print 'total num: %d, detected num: %d, valid subjects: %d' %(num_pic, num_detected, len(list_valid_person_id))
   
   
   
   
#   img_id = int(filename.split('-')[1].split('.')[0])
#   print '%d' %person_id
   
#  
   
   
   
#   print '%d' %person_id
#   new_directory = '%s%d/' %(OUTPUT_PATH+"ear_data/", person_id)
#   new_file_path = '%s%d%s' %(new_directory,img_id,'.jpg')
#   makedir(new_directory)
#   print "%s%s%s\n" % (BASE_PATH+'/'+filename, SEPARATOR, new_file_path)   
#   image = cv2.cvtColor(cv2.imread(BASE_PATH+'/'+filename),cv2.COLOR_RGB2GRAY)
#   cv2.imwrite(new_file_path, image)   

   
   
   



