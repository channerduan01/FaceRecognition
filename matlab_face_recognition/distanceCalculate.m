function distance = distanceCalculate(testSample, base, mean_img, train_data_on_base, train_data_index)
if ndims(train_data_on_base) == 2
    res = (testSample-mean_img) * base - train_data_on_base(train_data_index,:);
%     distance = sqrt(sum(res.^2));
    
    distance = sum(abs(res));
    
else
    res = testSample * base - train_data_on_base(:,:,train_data_index);
%     distance = sum(sqrt(sum(res.^2, 1)));
    distance = sum(sum(abs(res), 1));
    
    
end