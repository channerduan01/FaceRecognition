function dist = distanceEuc(features,samples_num,subject1,sample1,subject2,sample2)
a = featurevector(features,samples_num,subject1,sample1);
b = featurevector(features,samples_num,subject2,sample2);
dist = sqrt(sum((a-b).^2));
    