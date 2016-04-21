%% Load Images from ATT Face Database
faceDataBase = imageSet('/Users/channerduan/Documents/study/Face_Recog/att_orl_faces', 'recursive');

%% Display Montage of First Face
figure;
montage(faceDataBase(10).ImageLocation);
title('Images of Single Face');

%% Display Query Image and Database Side-Side
personToQuery = 1;
galleryImage = read(faceDataBase(personToQuery),1);
figure;


