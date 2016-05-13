function [features] = removevector(features,samples_num,subject,sample)
if ndims(features) == 2
    features(:, (subject-1)*samples_num+sample) = Inf;
else
    features(:, :, (subject-1)*samples_num+sample) = Inf;
end
