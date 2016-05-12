function fv = addvector(features,sample)
[~, cols] = size(features);
fv=features;
for i=1:cols
    if features(1,i) == Inf
        fv(:,i) = sample;
    end
end