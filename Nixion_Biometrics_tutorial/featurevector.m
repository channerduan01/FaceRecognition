function vec = featurevector(features,samples_num,subject_index,sample_index)
vec = features(subject_index, (sample_index-1)*samples_num+1:sample_index*samples_num);
end