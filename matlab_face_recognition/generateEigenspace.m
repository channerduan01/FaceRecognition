function base = generateEigenspace(select_energy, image_matrix)
    mean_img = mean(image_matrix);
    tmp_image_matrix = zeros(size(image_matrix));
    for i=1:size(image_matrix,1)
        tmp_image_matrix(i, :) = image_matrix(i, :) - mean_img;
    end
    C = tmp_image_matrix*tmp_image_matrix';
    [V, D]=eig(C);
    eigen_vector = flip(V,2);
    eigen_values = flip(diag(D));
    sum_eigen_values = sum(eigen_values);
    select_num = 1;
    while(sum(eigen_values(1:select_num))/sum_eigen_values < select_energy)
        select_num = select_num+1;
    end
    select_num = select_num-1;
    i = 1;
    base = zeros([size(mean_img,2), select_num]);
    while(i <= select_num)
        base(:, i) = eigen_values(i)^(-1/2) * tmp_image_matrix' * eigen_vector(:, i);
        i = i+1;
    end
end