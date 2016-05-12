function [eigen_vectors,eigen_values] = selectEigenvectors(covarianceMatrix, select_energy)
    [V, D]=eig(covarianceMatrix);
    eigen_vectors = flip(V,2);
    eigen_values = flip(diag(D));
    sum_eigen_values = sum(eigen_values);
    select_num = 1;
    while(sum(eigen_values(1:select_num))/sum_eigen_values < select_energy)
        select_num = select_num+1;
    end
    select_num = select_num-1;
    eigen_vectors = eigen_vectors(:, 1:select_num);
    eigen_values = eigen_values(1:select_num);    
end