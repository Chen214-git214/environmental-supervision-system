package com.neusoft.supervision.statistics.dto;

public class AqiDistributionResponse {
    private String level;
    private long count;

    public AqiDistributionResponse() {
    }

    public AqiDistributionResponse(String level, long count) {
        this.level = level;
        this.count = count;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
