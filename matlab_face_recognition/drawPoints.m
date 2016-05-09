function drawPoints(subjects_num, images)
    figure;
    hold on;
    samples_num = floor(size(images,1)/subjects_num);
    for i=1:subjects_num
        drawOneCluster(i,images((i-1)*samples_num+1:i*samples_num,:));
    end
    hold off;
end

function drawOneCluster(index, points)
% mark = strcat(colors{mod(index-1,length(colors))+1}, ...
%     original_marks{floor(index/length(colors))+1});
    colors = {'b','r','g','c','m','y'};
    original_marks = {'x','o','+','s','^','d','.'};
    mark_v = 1;
    if index > 90
       mark_v = mark_v +  index - 90;
    end
    mark = strcat(colors{mod(index-1,length(colors))+1},original_marks{mark_v});
    plot(points(:,1),points(:,2),mark);
end