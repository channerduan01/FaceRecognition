function [base, projection] = generate2DEigenspace(select_energy, image_matrix)
mean_images = mean(image_matrix,3);
G = zeros(size(image_matrix,2));
for i = 1:size(image_matrix,3)
    tmpX = (image_matrix(:,:,i) - mean_images);
    G = G + tmpX'*tmpX;
end
G = G/size(image_matrix,3);
[base, ~] = selectEigenvectors(G, select_energy);
projection = projectToBase2D(base, image_matrix);