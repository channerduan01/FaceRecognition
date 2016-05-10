function [distancebetweensamples] = withinclass(features,samples_num)
[rows, ~] = size(features);
distancebetweensamples = zeros(25,1);
index = 1;
for i=1:rows %% for same subjects 
    for i1=1:samples_num %% and all samples
        for j1=1:samples_num
            if (i1~=j1) %% but not for same samples
                distancebetweensamples(index)=distanceEuc(features,samples_num,i,i1,i,j1);
                index = index + 1;
            end
        end
    end
end
