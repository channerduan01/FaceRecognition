function projection = projectToBase2D(base, image_matrix)
projection = zeros(size(image_matrix,1), size(base,2), size(image_matrix,3));
for i = 1:size(image_matrix,3)
    projection(:,:,i) = image_matrix(:,:,i) * base;
end

