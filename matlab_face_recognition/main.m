%% init
close all
clear
clc
%% Load Images from ATT Face Database
% faceDatabase = imageSet('/Users/channerduan/Documents/study/Face_Recog/att_orl_faces', 'recursive');
% TRAIN_PART = 0.8;
faceDatabase = imageSet('/Users/channerduan/Documents/study/Face_Recog/Data/ear_data', 'recursive');
TRAIN_PART = 0.8;

[training, test] = partition(faceDatabase, [TRAIN_PART (1-TRAIN_PART)]);
subjects_num = length(faceDatabase);

% figure;
% montage(faceDatabase(16).ImageLocation);
% title('Images of Single Face');

[all_train_img, all_train_lable] = dataLoad(training); % load data
base = generateEigenspace(0.9, all_train_img); % calculate eigen vectors (based on dual problem)
all_train_img_on_base = all_train_img * base; % project to base
drawPoints(subjects_num, all_train_img_on_base); % show the distribution based on new base
KNN_test(1, base, all_train_img_on_base, test, 1:subjects_num)


%% Real Test
faceDatabase_ = imageSet('/Users/channerduan/Documents/study/Face_Recog/Data/Android_ear_cropped_test/arranged', 'recursive');
TRAIN_PART = 0.8;

[training_, test_] = partition(faceDatabase_, [TRAIN_PART (1-TRAIN_PART)]);
subjects_num_=length(faceDatabase_);

all_train_img_ = dataLoad(training_);
all_train_img_on_base_ = all_train_img_ * base;

drawPoints(subjects_num_, all_train_img_on_base_);

KNN_test(1, base, all_train_img_on_base_, test_, 1:subjects_num_)

%% Check
drawPoints(subjects_num+subjects_num_, [all_train_img_on_base;all_train_img_on_base_]);











