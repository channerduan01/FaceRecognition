function vec = featurevector(features,samples_num,subject_index,sample_index)
vec = features(:, (subject_index-1)*samples_num+sample_index);
end