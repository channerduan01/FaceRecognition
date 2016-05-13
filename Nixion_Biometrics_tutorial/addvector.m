function fv = addvector(features,sample)
fv = features;
if ndims(features) == 2
    for i=1:size(features,2)
        if features(1,i) == Inf
            fv(:,i) = sample;
            break;
        end
    end
else
    for i=1:size(features,3)
        if features(1,1,i) == Inf
            fv(:,:,i) = sample;
            break;
        end
    end
end

