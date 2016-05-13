function [distances] = betweenclass(features,samples_num)
if ndims(features) == 2
    subjects_num = size(features,2)/samples_num;
else
    subjects_num = size(features,3)/samples_num;
end
distances = zeros(1,subjects_num*(subjects_num-1)*samples_num*samples_num);
index = 1;
for i=1:subjects_num %% all subjects
    for j=1:subjects_num
        if (i == j), continue; end
%         fprintf('%d -> %d\n',i,j) 
        for i1=1:samples_num %% and all samples
            for j1=1:samples_num
                distances(index)=distanceEuc(features,samples_num,i,i1,j,j1);
                index = index + 1; 
            end
        end
    end
end