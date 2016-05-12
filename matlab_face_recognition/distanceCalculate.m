function distance = distanceCalculate(testSample, base, train_data_on_base, train_data_index)
if ndims(train_data_on_base) == 2
    res = testSample * base - train_data_on_base(train_data_index,:);
    distance = sqrt(sum(res.^2));
else
    res = testSample * base - train_data_on_base(:,:,train_data_index);
    distance = sum(sqrt(sum(res.^2, 1)));
end