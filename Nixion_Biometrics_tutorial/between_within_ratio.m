function [ratios] = between_within_ratio(features,samples_num)
if ndims(features) == 2
    subjects_num = size(features,2)/samples_num;
else
    subjects_num = size(features,3)/samples_num;
end
average_within_dists = zeros(1,subjects_num);
average_between_dists = zeros(1,subjects_num);
for i=1:subjects_num %% for same subjects
    total_dist = 0;
    for i1=1:samples_num %% and all samples
        for j1=1:samples_num
            if (i1~=j1) %% but not for same samples
                total_dist = total_dist+distanceEuc(features,samples_num,i,i1,i,j1);
            end
        end
    end
    average_within_dists(i) = total_dist / (samples_num*(samples_num-1));
    total_dist = 0;
    for j=1:subjects_num
        if (i == j), continue; end
        for i1=1:samples_num %% and all samples
            for j1=1:samples_num
                total_dist = total_dist+distanceEuc(features,samples_num,i,i1,j,j1);
            end
        end
    end    
    average_between_dists(i) = total_dist / (samples_num*samples_num*(subjects_num-1));   
end

ratios = average_between_dists./average_within_dists;