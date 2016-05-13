function vec = featurevector(features,samples_num,subject_index,sample_index)
if ndims(features) == 2
    vec = features(:, (subject_index-1)*samples_num+sample_index);
else
    vec = features(:, :, (subject_index-1)*samples_num+sample_index);
end