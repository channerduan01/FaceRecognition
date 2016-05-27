%% init for 2d real data sources
samples_num = size(all_train_img_on_base_2d, 3)/subjects_num;
features = all_train_img_on_base_2d;

%% data analysis
disp('So the feature vector for subject 2 sample 2 is')
featurevector(features,samples_num,2,2);
disp('and the distance between subject 1 sample 1 and subject 2 sample 1 is')
distanceEuc(features,samples_num,1,1,2,1)
between_dists = betweenclass(features,samples_num);
within_dists = withinclass(features,samples_num);
disp('Let us see the distributions of distances')
drawDistanceAnalysis(4, 'Distances 2D-PCA', between_dists, within_dists);
% between_within_ratio(features,samples_num)

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
drawCumulativeMatchScore(5, 'Cumulative 2D-PCA', ranked);


%% verification
threshold = 10000;
disp('Let us verif subject 4 sample 2')
b = featurevector(features,samples_num,4,2);
verify(features,samples_num,b,threshold)
disp('now let us add in a false subject')
% b = ones([size(features,1),1])*999;
b = ones([size(features,1),size(features,2)])*999;
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
% roc_rates = roc_rates/(subjects_num*samples_num)*100;
% % drawROC(roc_rates(2,:),roc_rates(3,:),'false accept vs false reject','false accept','false reject');
% drawROC(roc_rates(2,:),roc_rates(1,:),'ROC 2D-PCA','false positive','true possitive');
















