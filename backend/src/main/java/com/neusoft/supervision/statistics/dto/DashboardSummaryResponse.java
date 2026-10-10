package com.neusoft.supervision.statistics.dto;

public class DashboardSummaryResponse {
    private long totalFeedback;
    private long completedFeedback;
    private long todayFeedback;
    private double completionRate;

    public long getTotalFeedback() {
        return totalFeedback;
    }

    public void setTotalFeedback(long totalFeedback) {
        this.totalFeedback = totalFeedback;
    }

    public long getCompletedFeedback() {
        return completedFeedback;
    }

    public void setCompletedFeedback(long completedFeedback) {
        this.completedFeedback = completedFeedback;
    }

    public long getTodayFeedback() {
        return todayFeedback;
    }

    public void setTodayFeedback(long todayFeedback) {
        this.todayFeedback = todayFeedback;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }
}
