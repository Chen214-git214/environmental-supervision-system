package com.neusoft.supervision.feedback.repository;

public interface MonthlyTrendProjection {
    Integer getYearValue();

    Integer getMonthValue();

    Long getFeedbackCount();

    Long getCompletedCount();

    Long getPm25ExceedCount();

    Long getSo2ExceedCount();

    Long getCoExceedCount();
}
