function [distances] = betweenclass(features,samples_num)
[rows, cols] = size(features);
distances = zeros(cols*(cols-1)-30,1);
index = 1;
for i=1:rows %% all subjects
    for j=1:rows
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