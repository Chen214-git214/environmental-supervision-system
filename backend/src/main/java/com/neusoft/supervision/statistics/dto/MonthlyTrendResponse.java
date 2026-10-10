package com.neusoft.supervision.statistics.dto;

public class MonthlyTrendResponse {
    private String month;
    private long feedbackCount;
    private long completedCount;
    private long pm25ExceedCount;
    private long so2ExceedCount;
    private long coExceedCount;

    public MonthlyTrendResponse() {
    }

    public MonthlyTrendResponse(String month) {
        this.month = month;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getFeedbackCount() {
        return feedbackCount;
    }

    public void setFeedbackCount(long feedbackCount) {
        this.feedbackCount = feedbackCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getPm25ExceedCount() {
        return pm25ExceedCount;
    }

    public void setPm25ExceedCount(long pm25ExceedCount) {
        this.pm25ExceedCount = pm25ExceedCount;
    }

    public long getSo2ExceedCount() {
        return so2ExceedCount;
    }

    public void setSo2ExceedCount(long so2ExceedCount) {
        this.so2ExceedCount = so2ExceedCount;
    }

    public long getCoExceedCount() {
        return coExceedCount;
    }

    public void setCoExceedCount(long coExceedCount) {
        this.coExceedCount = coExceedCount;
    }
}
