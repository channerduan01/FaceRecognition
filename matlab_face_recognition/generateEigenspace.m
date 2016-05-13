function [base, projection] = generateEigenspace(select_energy, image_matrix)
    mean_img = mean(image_matrix);
    tmp_image_matrix = zeros(size(image_matrix));
    for i=1:size(image_matrix,1)
        tmp_image_matrix(i, :) = image_matrix(i, :) - mean_img;
    end
    D = tmp_image_matrix*tmp_image_matrix';
    [eigen_vectors, eigen_values] = selectEigenvectors(D, select_energy);  
    base = zeros(size(mean_img,2), size(eigen_vectors,2));
    for i=1:size(eigen_vectors,2)
        % convert back from dual space
        base(:, i) = eigen_values(i)^(-1/2) * tmp_image_matrix' * eigen_vectors(:, i);
    end
    projection = image_matrix * base;
end