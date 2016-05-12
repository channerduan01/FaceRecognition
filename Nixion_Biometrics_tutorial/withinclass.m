function [distancebetweensamples] = withinclass(features,samples_num)
[~, cols] = size(features);
subjects_num = cols/samples_num;
distancebetweensamples = zeros(1,subjects_num*samples_num*(samples_num-1));
index = 1;
for i=1:subjects_num %% for same subjects 
    for i1=1:samples_num %% and all samples
        for j1=1:samples_num
            if (i1~=j1) %% but not for same samples
                distancebetweensamples(index) = distanceEuc(features,samples_num,i,i1,i,j1);
                index = index + 1;
            end
        end
    end
end
