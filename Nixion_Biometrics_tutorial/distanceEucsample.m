function dist = distanceEucsample(features,samples_num,subject1,sample1,unknownsample)
a = featurevector(features,samples_num,subject1,sample1);
b = unknownsample;
dist = sqrt(sum((a-b).^2));

    