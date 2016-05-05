import os.path


BASE_PATH="/Users/channerduan/Desktop/Biometrics/DatabaseEars_Southampton/DatabaseEarsCropped"
SEPARATOR=";"

for filename in os.listdir(BASE_PATH):
   if filename.find('.jpg') == -1:
       continue
   person_id = int(filename.split('-')[0])
#   print '%s %d' %(filename, filename.find('.pgm'))
   print "%s%s%d" % (BASE_PATH+'/'+filename, SEPARATOR, person_id)


