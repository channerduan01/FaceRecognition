function [features] = removevector(features,samples_num,subject,sample)
features(:, (subject-1)*samples_num+sample) = Inf;