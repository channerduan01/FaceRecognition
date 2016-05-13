function [rate] = recognitionrate(features,samples_num)
if ndims(features) == 2
    subjects_num = size(features,2)/samples_num;   
else
    subjects_num = size(features,3)/samples_num;  
end
correct = 0;
error = 0;
for i=1:subjects_num
    for j=1:samples_num
        takenout = featurevector(features,samples_num,i,j);
        features = removevector(features,samples_num,i,j);
        recognised_subject = recognisesample(features,samples_num,takenout);
        if (recognised_subject == i)
            correct = correct+1;
        else
%             fprintf('error case:%d-%d\n', i,j);
            error = error+1;
        end
        features = addvector(features,takenout);  
    end 
end
rate = correct/(correct+error)*100;

