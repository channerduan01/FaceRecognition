function classify = recognisesample(features, samples_num, sample)
if ndims(features) == 2
    subjects_num = size(features,2)/samples_num;   
else
    subjects_num = size(features,3)/samples_num;  
end
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