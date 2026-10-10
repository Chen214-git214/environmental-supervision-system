package com.neusoft.supervision.statistics.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardResponse {
    private DashboardSummaryResponse summary;
    private List<ProvincePollutantStatResponse> provincePollutantStats;
    private List<AqiDistributionResponse> aqiDistribution;
    private List<MonthlyTrendResponse> monthlyTrend;
    private LocalDateTime refreshedAt;

    public DashboardSummaryResponse getSummary() {
        return summary;
    }

    public void setSummary(DashboardSummaryResponse summary) {
        this.summary = summary;
    }

    public List<ProvincePollutantStatResponse> getProvincePollutantStats() {
        return provincePollutantStats;
    }

    public void setProvincePollutantStats(List<ProvincePollutantStatResponse> provincePollutantStats) {
        this.provincePollutantStats = provincePollutantStats;
    }

    public List<AqiDistributionResponse> getAqiDistribution() {
        return aqiDistribution;
    }

    public void setAqiDistribution(List<AqiDistributionResponse> aqiDistribution) {
        this.aqiDistribution = aqiDistribution;
    }

    public List<MonthlyTrendResponse> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<MonthlyTrendResponse> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }

    public LocalDateTime getRefreshedAt() {
        return refreshedAt;
    }

    public void setRefreshedAt(LocalDateTime refreshedAt) {
        this.refreshedAt = refreshedAt;
    }
}
