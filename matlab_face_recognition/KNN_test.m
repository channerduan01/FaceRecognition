function accuracy = KNN_test(k, threshold, base, mean_img, train_data_on_base, test_database, test_subject_range)
    if ndims(train_data_on_base) == 2   % compatibility design
        train_items_num = size(train_data_on_base, 1);
    else
        train_items_num = size(train_data_on_base, 3);
    end
    dists = zeros(train_items_num,1);
    subjects_num = length(test_database);
    samples_test_num = test_database(1).Count;
    samples_train_num = train_items_num/subjects_num;
    correct_cases = 0;
    total_cases = 0;
    for i = test_subject_range
       for j=1:samples_test_num
           total_cases = total_cases+1;
           if ndims(train_data_on_base) == 2   % compatibility design
               test_img = double(reshape(read(test_database(i),j), 1, []));
           else
               test_img = double(read(test_database(i),j));
           end
           for ii=1:length(dists)
               dists(ii) = distanceCalculate(test_img, base, mean_img, train_data_on_base, ii);   % compatibility design
           end
           [dists, index] = sort(dists);
           if threshold ~= 0 && dists(1) > threshold
                continue;
           end
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