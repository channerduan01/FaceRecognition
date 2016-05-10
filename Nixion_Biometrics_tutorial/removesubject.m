function [features] = removesubject(features,subject)
features(subject,:) = Inf;
