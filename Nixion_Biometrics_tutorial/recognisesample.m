function classify = recognisesample(features, samples_num, sample)
[rows, ~] = size(features);
min = Inf;
for i=1:rows %% all subjects
    for i1=1:samples_num %% and all samples
            distance=distanceEucsample(features,samples_num,i,i1,sample);
                if distance < min
                    min = distance;
                    classify = i; 
                end;
    end
end