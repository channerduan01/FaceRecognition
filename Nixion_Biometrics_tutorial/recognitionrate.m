function [rate] = recognitionrate(features,samples_num)
[rows, ~] = size(features);
correct = 0;
error = 0;
for i=1:rows
    for j=1:samples_num
        takenout = featurevector(features,samples_num,i,j);
        features = removevector(features,samples_num,i,j);
        recognised_subject = recognisesample(features,samples_num,takenout);
        if (recognised_subject == i)
            correct = correct+1;
        else
            error = error+1;
        end
        features = addvector(features,takenout);  
    end 
end
rate = correct/(correct+error)*100;

