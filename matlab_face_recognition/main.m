%% init
close all
clear
clc
% Load Images from ATT Face Database
% faceDatabase = imageSet('/Users/channerduan/Documents/study/Face_Recog/att_orl_faces', 'recursive');
% TRAIN_PART = 0.8;
faceDatabase = imageSet('/Users/channerduan/Documents/study/Face_Recog/Data/ear_data', 'recursive');
TRAIN_PART = 0.5;
% figure;
% montage(faceDatabase(16).ImageLocation);
% title('Images of Single Face');

[training, test] = partition(faceDatabase, [TRAIN_PART (1-TRAIN_PART)]);
subjects_num = length(faceDatabase);
%% classic PCA
[all_train_img, ~] = dataLoad(training,true); % load data
%%
tic;
[base, mean_img, all_train_img_on_base] = generateEigenspace(0.95, all_train_img); % calculate eigen vectors (based on dual problem)
toc
drawPoints(subjects_num, all_train_img_on_base); % show the distribution based on new base
tic;
KNN_test(1, 0, base, mean_img, all_train_img_on_base, test, 1:subjects_num)
toc
%% 2D PCA
[all_train_img_2d, ~] = dataLoad(training,false);
%%
tic;
[base_2d, all_train_img_on_base_2d] = generate2DEigenspace(0.90, all_train_img_2d);
toc
tic;
KNN_test(1, 0, base_2d, eye(1), all_train_img_on_base_2d, test, 1:subjects_num)
toc

%%
% check_test = imageSet('/Users/channerduan/Documents/study/Face_Recog/Data/Android_ear_cropped_test/arranged/iii');
% [check_test_img, ~] = dataLoad(check_test,false);
% check_test_img_on_2d = projectToBase2D(base_2d, check_test_img);


% %% Real Test
% training_ = imageSet('/Users/channerduan/Documents/study/Face_Recog/Data/Android_ear_cropped_test/arranged', 'recursive');
% test_ = imageSet('/Users/channerduan/Documents/study/Face_Recog/Data/Android_ear_cropped_test1/arranged', 'recursive');
% % TRAIN_PART = 0.1;
% % [training_, test_] = partition(faceDatabase_, [TRAIN_PART (1-TRAIN_PART)]);
% 
% 
% subjects_num_ = length(faceDatabase_);
% 
% all_train_img_ = dataLoad(training_,true);
% all_test_img_ = dataLoad(test_,true);
% all_train_img_on_base_ = all_train_img_ * base;
% all_test_img_on_base_ = all_test_img_ * base;
% KNN_test(1, 2000, base, all_train_img_on_base_, test_, 1:subjects_num_)
% % all_train_img_on_base = all_train_img_on_base_;
% drawPoints(subjects_num_, all_train_img_on_base_);
% drawPoints(subjects_num_, all_test_img_on_base_);



% all_train_img_ = dataLoad(training_,false);
% all_train_img_on_base_ = projectToBase2D(base_2d, all_train_img_);
% KNN_test(1, 5000, base_2d, all_train_img_on_base_, test_, 1:subjects_num_)
% all_train_img_on_base_2d = all_train_img_on_base_;


% %% Check
% drawPoints(subjects_num_, all_train_img_on_base_);
% drawPoints(subjects_num+subjects_num, [all_train_img_on_base;all_train_img_on_base_]);



