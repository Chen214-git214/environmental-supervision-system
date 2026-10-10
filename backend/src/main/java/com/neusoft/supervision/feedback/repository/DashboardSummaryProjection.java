package com.neusoft.supervision.feedback.repository;

public interface DashboardSummaryProjection {
    Long getTotalFeedback();

    Long getCompletedFeedback();

    Long getTodayFeedback();
}
