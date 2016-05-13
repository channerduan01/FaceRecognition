function [features] = removesubject(features,samples_num,subject)
if ndims(features) == 2
    features(:, (subject-1)*samples_num+1:subject*samples_num) = Inf;
else
    features(:, :, (subject-1)*samples_num+1:subject*samples_num) = Inf;
end

