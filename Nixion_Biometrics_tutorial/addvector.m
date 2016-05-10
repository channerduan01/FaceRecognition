function fv = addvector(features,sample)
[rows, cols] = size(features);
num = length(sample);
fv=features;
for i=1:rows
    for j=1:cols
        if features(i,j) == Inf
            fv(i,j:j+num-1) = sample;
            return;
        end
    end
end