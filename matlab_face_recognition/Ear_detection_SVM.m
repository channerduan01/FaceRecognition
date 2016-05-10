%% init
close all
clear
clc

%% window size 
window_row = 55;
window_col = 35;
%% Cropped ear from Soton database (Positive img)
% croppedEar = imageSet('/Users/channerduan/Documents/study/Face_Recog/Data/Ear_cropped_by_myself');
croppedEar = imageSet('/Users/channerduan/Documents/study/Face_Recog/DatabaseEars_Southampton/DatabaseEarsCropped');

len_soton = croppedEar.Count;
crop = {};
for i = 1:len_soton
    graycrop = imresize(rgb2gray(read(croppedEar,i)), [window_row window_col]);
    % resized gray img [col=1]
    crop{i,1} = double(reshape(graycrop, 1, []));
    % HOG feature [col=2]
    crop{i,2} = extractHOGFeatures(graycrop);
    % label = true [col=3]
    crop{i,3} = 1;
end
%% (negative img) 
negative = imageSet('/Users/channerduan/Documents/study/Face_Recog/Haar/negatives');
len_negative = negative.Count;
nega = {};
for i = 1:len_negative
    graynega = imresize(read(negative,i), [window_row window_col]);
    % resized gray img [col=1]
    nega{i,1} = double(reshape(graynega, 1, []));
    % HOG feature [col=2]
    nega{i,2} = extractHOGFeatures(graynega);
    % label = false [col=3]
    nega{i,3} = 0;
end
%% Tr,Ts partition
all = cat(1,crop,nega);% concatenate positive and negative data together 
[len_all,~] = size(all);
c = cvpartition(len_all,'HoldOut',0.1);% partition into tr(0.9) and ts(0.1)
trIdx = c.training;
tsIdx = c.test;
trainingFeatures = cell2mat(all(trIdx,1));
trainingLabels = cell2mat(all(trIdx,3));
testFeatures = cell2mat(all(tsIdx,1));
testLabels = cell2mat(all(tsIdx,3));
%% Classifier SVM
classifier = fitcecoc(trainingFeatures,trainingLabels);
estLabels = predict(classifier,testFeatures);
%% Result
tf = testLabels - estLabels;
correctRate = length(find(tf == 0))/size(estLabels,1);
display(['correctRate: ',num2str(correctRate)]);

