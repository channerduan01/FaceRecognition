function accuracy = KNN_test(k, base, train_data_on_base, test_database, test_subject_range)
    dists = zeros(size(train_data_on_base,1),1);
    subjects_num = length(test_database);
    samples_test_num = test_database(1).Count;
    samples_train_num = floor(size(train_data_on_base,1)/subjects_num);
    correct_cases = 0;
    total_cases = 0;
    for i = test_subject_range
       for j=1:samples_test_num
           total_cases = total_cases+1;
           test_img = double(reshape(read(test_database(i),j), 1, []));
           test_img_on_base = test_img * base;
           for ii=1:length(dists)
               dists(ii) = norm(test_img_on_base - train_data_on_base(ii,:));
           end
           [dists, index] = sort(dists);
           map = zeros(1,subjects_num);
           for ii=1:k
               face_idx = floor((index(ii)-1)/samples_train_num)+1;
               map(face_idx) = map(face_idx)+1;
           end
           [~, face_idx] = max(map);
           if face_idx == i
               correct_cases = correct_cases+1;
           end
       end
    end
%     correct_cases
    accuracy = correct_cases/total_cases;
end