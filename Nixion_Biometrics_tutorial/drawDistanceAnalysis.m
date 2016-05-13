function drawDistanceAnalysis(draw_id, title_, between, within)
figure(draw_id), clf;
hold on
histogram(between)
histogram(within)
hold off
set(gca,'FontSize',16);
xlabel('distance', 'FontSize', 16);
ylabel('number', 'FontSize', 16);
title(title_, 'FontSize', 20)
legend('between-class', 'within-class');