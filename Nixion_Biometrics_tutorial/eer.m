function [results] = eer(features, samples_num, threshold)
%[tar,far,frr,trr]
[~, cols] = size(features);
subjects_num = cols/samples_num;
% initialise rates to nothing
results = zeros(4,1);
for i=1:subjects_num
    for j=1:samples_num
        takenout = featurevector(features,samples_num,i,j);
        features = removevector(features,samples_num,i,j);
        outcome = verify(features,samples_num,takenout,threshold);
        if (outcome == 1 && recognisesample(features,samples_num,takenout) == i) 
            results(1) = results(1)+1; %tar
        else
            if (outcome == 1 && recognisesample(features,samples_num,takenout) ~= i)
                results(2) = results(2)+1; %far
            else
                if (outcome == 0 && recognisesample(features,samples_num,takenout) == i)
                results(3) = results(3)+1; %frr
                else
                    if (outcome == 0 && recognisesample(features,samples_num,takenout) ~= i)
                        results(4) = results(4)+1; %trr
                    end
                end
            end
        end    
        features = addvector(features,takenout);
    end
end
  



