function classify = recognisesample(features, samples_num, sample)
[~, cols] = size(features);
subjects_num = cols/samples_num;
min = Inf;
for i=1:subjects_num %% all subjects
    for i1=1:samples_num %% and all samples
            distance = distanceEucsample(features,samples_num,i,i1,sample);
            if distance < min
            	min = distance;
            	classify = i; 
            end;
    end
end