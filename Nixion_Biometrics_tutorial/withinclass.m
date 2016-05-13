function [distancebetweensamples] = withinclass(features,samples_num)
if ndims(features) == 2
    subjects_num = size(features,2)/samples_num;
else
    subjects_num = size(features,3)/samples_num;
end
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
