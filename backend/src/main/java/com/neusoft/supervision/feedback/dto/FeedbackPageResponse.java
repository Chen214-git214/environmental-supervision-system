package com.neusoft.supervision.feedback.dto;

import java.util.List;

public class FeedbackPageResponse<T> {
    private List<T> records;
    private long total;

    public FeedbackPageResponse() {
    }

    public FeedbackPageResponse(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
