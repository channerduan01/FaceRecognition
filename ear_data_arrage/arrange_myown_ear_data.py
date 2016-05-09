import os.path
import cv2
import numpy as np
import shutil

BASE_PATH="/Users/channerduan/Documents/study/Face_Recog/Data/Ear_cropped_by_myself"
OUTPUT_PATH="/Users/channerduan/Documents/study/Face_Recog/Data/"
SEPARATOR=";"


def makedir(path):
    if (os.path.exists(path) == False):
        os.mkdir(path)

map_person = np.zeros(200)
dict_ = {}
makedir(OUTPUT_PATH+"/ear_data")
for filename in os.listdir(BASE_PATH):
   if filename.find('.jpg') == -1:
       continue
   person_id = int(filename.split('-')[0])
   source_id = int(filename.split('-')[1])  
   cropped_id = int(filename.split('-')[2])
   if dict_.has_key(person_id) == False:
       dict_[person_id] = []
   dict_[person_id].append((source_id,cropped_id))
   map_person[person_id] = map_person[person_id]+1
#   new_directory = '%s%d/' %(OUTPUT_PATH+"ear_data/", person_id)
#   new_file_path = '%s%d%s' %(new_directory,img_id,'.jpg')
#   print "%s%s%s\n" % (BASE_PATH+'/'+filename, SEPARATOR, new_file_path)   
   #   makedir(new_directory)
#   image = cv2.cvtColor(cv2.imread(BASE_PATH+'/'+filename),cv2.COLOR_RGB2GRAY)
#   cv2.imwrite(new_file_path, image)   
for key in dict_.keys():
   np.random.shuffle(dict_[key])


THRESHOULD = 10 # key point!!!
FORM_WIDTH = 47
FORM_HEIGHT = 78

indices = np.where(map_person>=THRESHOULD)[0]
new_index = 1
all_width = np.zeros(len(indices)*THRESHOULD)
all_height = np.zeros(len(indices)*THRESHOULD)

for i in indices:
    new_directory = '%s%d/' %(OUTPUT_PATH+"ear_data/", new_index)
    makedir(new_directory)
    list_ = dict_[i]
    for j in range(THRESHOULD):
        (s_id, c_id) = list_[j]
        orig_filename = '%d-%d-%d-.jpg' %(i, s_id, c_id)
        new_file_path = '%s%d%s' %(new_directory,j+1,'.jpg')
#        shutil.copy(BASE_PATH+'/'+orig_filename,  new_file_path)
        image = cv2.cvtColor(cv2.imread(BASE_PATH+'/'+orig_filename),cv2.COLOR_RGB2GRAY)
        image = cv2.resize(image,(FORM_WIDTH,FORM_HEIGHT),interpolation=cv2.INTER_CUBIC)
        (all_width[(new_index-1)*THRESHOULD+j], all_height[(new_index-1)*THRESHOULD+j]) = image.shape
        cv2.imwrite(new_file_path, image)
    new_index = new_index+1



















