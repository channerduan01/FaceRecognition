function [features] = removesubject(features,samples_num,subject)
features(:,(subject-1)*samples_num+1:subject*samples_num) = Inf;