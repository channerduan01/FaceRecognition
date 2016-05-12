function [rate] = recognitionrate(features,samples_num)
[~, cols] = size(features);
subjects_num = cols/samples_num;
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

