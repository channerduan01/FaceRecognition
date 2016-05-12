function [rate] = recognitionrateranked(features,samples_num)
[~, cols] = size(features);
subjects_num = cols/samples_num;
rate = zeros (subjects_num,1) ; % initialise rates to nothing
% test_v = 0;
for i=1:subjects_num
    for j=1:samples_num
        takenout = featurevector(features,samples_num,i,j);
        features = removevector(features,samples_num,i,j);
        recognised_subject = recognisesample(features,samples_num,takenout);
        k = 1;
        features1 = features;
        while(true)
%             if (k == 2)
%                 test_v = test_v+1;
%                 fprintf('%d->%d\n',recognised_subject,i);
%             end
            if (recognised_subject == i) 
                rate(k) = rate(k)+1;  
                break;
            end
            k = k+1;
            features1 = removesubject(features1,samples_num,recognised_subject);
            recognised_subject = recognisesample(features1,samples_num,takenout);
        end
        features=addvector(features,takenout);  
    end
end
for i = 2:subjects_num
    rate(i)=rate(i)+rate(i-1);
end
rate=rate*100/(subjects_num*samples_num);
% fprintf('test!: %d\n', test_v);


