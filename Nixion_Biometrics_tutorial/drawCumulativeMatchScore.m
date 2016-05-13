function drawCumulativeMatchScore(draw_id, title_, ranked)
figure(draw_id), clf;
plot(ranked(:));
set(gca,'FontSize',16);
xlabel('rank', 'FontSize', 16);
ylabel('correct', 'FontSize', 16);
title(title_, 'FontSize', 20)
axis([1 15 95 100])