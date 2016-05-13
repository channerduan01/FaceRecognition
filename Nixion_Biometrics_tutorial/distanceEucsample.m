function dist = distanceEucsample(features,samples_num,subject1,sample1,unknownsample)
a = featurevector(features,samples_num,subject1,sample1);
b = unknownsample;
if ndims(features) == 2
    dist = sqrt(sum((a-b).^2));
else
    dist = sum(sqrt(sum((a-b).^2, 1)));
end

