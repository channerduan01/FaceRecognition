function [accept] = verify(features,samples_num,sample,threshold)
if ndims(features) == 2
    subjects_num = size(features,2)/samples_num;   
else
    subjects_num = size(features,3)/samples_num;  
end
accept = 0;
for i=1:subjects_num
    for j=1:samples_num
        dist = distanceEucsample(features,samples_num,i,j,sample);
        if (dist < threshold && dist ~= 0) %is distance small and not same sample
            accept(1)=1; %record accept
        end  
    end 
end
