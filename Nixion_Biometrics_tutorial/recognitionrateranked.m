function [rate] = recognitionrateranked(features,samples_num)
[rows, ~] = size(features);
rate = zeros (rows,1) ; % initialise rates to nothing
for i=1:rows
    for j=1:samples_num
        takenout = featurevector(features,samples_num,i,j);
        features = removevector(features,samples_num,i,j);
        recognised_subject = recognisesample(features,samples_num,takenout);
        k = 1;
        features1 = features;
        while(true)
            if (k == rows || recognised_subject == i) 
                rate(k) = rate(k)+1;  
                break;
            end
            k = k+1;
            features1 = removesubject(features1,recognised_subject);
            recognised_subject = recognisesample(features1,samples_num,takenout);
        end
        features=addvector(features,takenout);  
    end
end
for i = 2:rows
    rate(i)=rate(i)+rate(i-1);
end
rate=rate*100/(rows*samples_num);


