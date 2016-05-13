function dist = distanceEuc(features,samples_num,subject1,sample1,subject2,sample2)
a = featurevector(features,samples_num,subject1,sample1);
b = featurevector(features,samples_num,subject2,sample2);
if ndims(features) == 2
    dist = sqrt(sum((a-b).^2));
else
    dist = sum(sqrt(sum((a-b).^2, 1)));
end


    