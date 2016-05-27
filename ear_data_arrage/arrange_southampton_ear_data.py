import os.path
import cv2

BASE_PATH="/Users/channerduan/Documents/study/Face_Recog/DatabaseEars_Southampton/DatabaseEarsCropped"
OUTPUT_PATH="/Users/channerduan/Documents/study/Face_Recog/Data"
SEPARATOR=";"


def makedir(path):
    if (os.path.exists(path) == False):
        os.mkdir(path)

makedir(OUTPUT_PATH+"ear_data")
for filename in os.listdir(BASE_PATH):
   if filename.find('.jpg') == -1:
       continue
   person_id = int(filename.split('-')[0])
   img_id = int(filename.split('-')[1].split('.')[0])
#   print '%d' %person_id
   new_directory = '%s%d/' %(OUTPUT_PATH+"ear_data/", person_id)
   new_file_path = '%s%d%s' %(new_directory,img_id,'.jpg')
   makedir(new_directory)
   print "%s%s%s\n" % (BASE_PATH+'/'+filename, SEPARATOR, new_file_path)   
   image = cv2.cvtColor(cv2.imread(BASE_PATH+'/'+filename),cv2.COLOR_RGB2GRAY)
   cv2.imwrite(new_file_path, image)   
   
   
   


