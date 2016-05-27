% %% init
% close all
% clear
% clc
% subjects_num=5;
% samples_num=3;
% features=[17.5 17.8 18.2 16.1 16.0 16.0 16.4 17.8 16.6 18.0 18.0 18.5 19.5 19.0 19.0;
%           10.0 10.3 10.4 10.4 9.5 9.5 9.5 9.7 9.8 10.5 10.0 10.0 11.0 11.0 11.0;
%           7.0 6.7 6.9 7.0 7.1 7.0 8.5 8.7 8.7 8.5 9.5 8.5 10.5 9.8 9.5;
%           7.5 7.7 7.7 6.3 6.2 6.3 6.5 6.5 6.5 6.5 7.0 7.0 7.2 7.5 7.5;
%           8.0 7.6 7.7 8.0 8.0 8.0 8.0 8.1 8.0 8.4 8.0 8.5 7.8 7.8 7.9;
%           1.9 2.0 3.3 1.9 1.9 1.9 1.9 2.8 2.0 2.5 3.2 3.0 2.0 2.0 2.2];
      
      

%% Special init for real data sources
samples_num = size(all_train_img_on_base, 1)/subjects_num;
features = all_train_img_on_base';

%% hear we shall plot some features
% figure()
% hold on
% scatter(features(1,:), features(2,:)); % this features are very similar
% scatter(features(6,:), features(1,:)); % these features are very different
% hold off

%% data analysis
disp('So the feature vector for subject 2 sample 2 is')
featurevector(features,samples_num,2,2);
disp('and the distance between subject 1 sample 1 and subject 2 sample 1 is')
distanceEuc(features,samples_num,1,1,2,1)
between_dists = betweenclass(features,samples_num);
within_dists = withinclass(features,samples_num);
disp('Let us see the distributions of distances')
drawDistanceAnalysis(1, 'Distances PCA', between_dists, within_dists);
between_within_ratio(features,samples_num)

%% recognition simple test
disp('Let us see subject 3 sample 2')
b = featurevector(features,samples_num,3,2);
features = removevector(features,samples_num,3,2);
disp('when we recognise the closest subject to the one we took out we get')
recognisesample(features,samples_num,b)
disp('recover the features')
features = addvector(features,b);
% let try something else
disp('Let us take subject 4 sample 3')
b = featurevector(features,samples_num,4,2);
features = removevector(features,samples_num,4,2);
recognisesample(features,samples_num,b)
features = addvector(features,b);

%% recognition over the whole database
disp('The recognition rate is')
recog = recognitionrate(features,samples_num)
% now let's look for the ranking
disp('The cumulative recognition is')
ranked = recognitionrateranked(features,samples_num)
drawCumulativeMatchScore(2, 'Cumulative PCA', ranked);

%% verification
threshold = 3000;
disp('Let us verif subject 4 sample 2')
b = featurevector(features,samples_num,4,2);
verify(features,samples_num,b,threshold)
disp('now let us add in a false subject')
b = ones([size(features,1),1])*999;
disp('and (fail) to verify')
verify(features,samples_num,b,threshold)

% %% ROC (Receiver Operating Characteristic curve)
% roc_samples = 100;
% roc_rates = zeros(4,roc_samples);
% for i=1:roc_samples
%     threshold = i/100*10000;
%     a = eer(features,samples_num,threshold);
%     roc_rates(:,i) = a(:);
% end
% 
% roc_rates = roc_rates/(subjects_num*samples_num)*100;
% drawROC(roc_rates(2,:),roc_rates(3,:),'false accept vs false reject','false accept','false reject');
% drawROC(roc_rates(2,:),roc_rates(1,:),'ROC PCA','false positive','true possitive');
















