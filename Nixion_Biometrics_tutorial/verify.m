function [accept] = verify(features,samples_num,sample,threshold)
[rows, ~] = size(features);
accept = 0;
for i=1:rows
    for j=1:samples_num
        dist = distanceEucsample(features,samples_num,i,j,sample);
        if (dist < threshold && dist ~= 0) %is distance small and not same sample
            accept(1)=1; %record accept
        end  
    end 
end
