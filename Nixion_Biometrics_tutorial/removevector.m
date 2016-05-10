function [features] = removevector(features,samples_num,subject,sample)
features(subject,(sample-1)*samples_num+1:sample*samples_num) = Inf;