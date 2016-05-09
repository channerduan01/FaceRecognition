function [images,lables] = dataLoad(database)
    subject_num = length(database);
    samples_num = database(1).Count;
    rows = size(read(database(1),1),1);
    columns = size(read(database(1),1),2);
    features_num = rows*columns;

    images = zeros(subject_num*samples_num, features_num);
    lables = zeros([1, 200],'int8');
    index = 1;
    for i=1:subject_num
        for j=1:samples_num
            img = reshape(read(database(i),j), 1, []);
            images(index, :) = img;
            lables(index) = i;
            index = index+1;
        end
    end
end