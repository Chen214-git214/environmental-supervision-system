package com.neusoft.supervision.statistics.service;

import com.neusoft.supervision.feedback.repository.AqiDistributionProjection;
import com.neusoft.supervision.feedback.repository.DashboardSummaryProjection;
import com.neusoft.supervision.feedback.repository.MonthlyTrendProjection;
import com.neusoft.supervision.feedback.repository.ProvincePollutantExceedanceProjection;
import com.neusoft.supervision.feedback.repository.PublicFeedbackRepository;
import com.neusoft.supervision.feedback.service.DictionaryService;
import com.neusoft.supervision.statistics.dto.AqiDistributionResponse;
import com.neusoft.supervision.statistics.dto.DashboardResponse;
import com.neusoft.supervision.statistics.dto.DashboardSummaryResponse;
import com.neusoft.supervision.statistics.dto.MonthlyTrendResponse;
import com.neusoft.supervision.statistics.dto.ProvincePollutantStatResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardStatisticsService {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PublicFeedbackRepository publicFeedbackRepository;
    private final DictionaryService dictionaryService;

    public DashboardStatisticsService(
            PublicFeedbackRepository publicFeedbackRepository,
            DictionaryService dictionaryService) {
        this.publicFeedbackRepository = publicFeedbackRepository;
        this.dictionaryService = dictionaryService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        DashboardResponse response = new DashboardResponse();
        response.setSummary(buildSummary());
        response.setProvincePollutantStats(buildProvincePollutantStats());
        response.setAqiDistribution(buildAqiDistribution());
        response.setMonthlyTrend(buildMonthlyTrend());
        response.setRefreshedAt(LocalDateTime.now());
        return response;
    }

    private DashboardSummaryResponse buildSummary() {
        DashboardSummaryProjection projection = publicFeedbackRepository.countDashboardSummary();
        long totalFeedback = valueOrZero(projection == null ? null : projection.getTotalFeedback());
        long completedFeedback = valueOrZero(projection == null ? null : projection.getCompletedFeedback());
        long todayFeedback = valueOrZero(projection == null ? null : projection.getTodayFeedback());
        DashboardSummaryResponse summary = new DashboardSummaryResponse();
        summary.setTotalFeedback(totalFeedback);
        summary.setCompletedFeedback(completedFeedback);
        summary.setTodayFeedback(todayFeedback);
        summary.setCompletionRate(totalFeedback == 0 ? 0 : round(completedFeedback * 100.0 / totalFeedback));
        return summary;
    }

    private List<ProvincePollutantStatResponse> buildProvincePollutantStats() {
        return publicFeedbackRepository.countProvincePollutantExceedance()
                .stream()
                .map(this::toProvincePollutantStat)
                .toList();
    }

    private ProvincePollutantStatResponse toProvincePollutantStat(ProvincePollutantExceedanceProjection projection) {
        ProvincePollutantStatResponse response = new ProvincePollutantStatResponse();
        response.setProvinceName(projection.getProvinceName());
        response.setProvinceShortName(projection.getProvinceShortName());
        response.setPm25ExceedCount(valueOrZero(projection.getPm25ExceedCount()));
        response.setSo2ExceedCount(valueOrZero(projection.getSo2ExceedCount()));
        response.setCoExceedCount(valueOrZero(projection.getCoExceedCount()));
        return response;
    }

    private List<AqiDistributionResponse> buildAqiDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        // 级别口径来自 aqi_level 字典表，新增级别无需改代码
        dictionaryService.aqiLevelNames().forEach(level -> distribution.put(level, 0L));

        publicFeedbackRepository.countAqiDistribution().forEach(item -> {
            String level = item.getLevel();
            if (level != null && distribution.containsKey(level)) {
                distribution.put(level, valueOrZero(item.getCount()));
            }
        });

        return distribution.entrySet()
                .stream()
                .map(entry -> new AqiDistributionResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<MonthlyTrendResponse> buildMonthlyTrend() {
        YearMonth currentMonth = YearMonth.now();
        Map<YearMonth, MonthlyTrendResponse> trendMap = new LinkedHashMap<>();
        YearMonth startMonth = currentMonth.minusMonths(5);

        for (int index = 5; index >= 0; index--) {
            YearMonth month = currentMonth.minusMonths(index);
            trendMap.put(month, new MonthlyTrendResponse(month.format(MONTH_FORMATTER)));
        }

        LocalDateTime startTime = startMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        for (MonthlyTrendProjection projection : publicFeedbackRepository.countMonthlyTrend(startTime, endTime)) {
            YearMonth month = YearMonth.of(projection.getYearValue(), projection.getMonthValue());
            MonthlyTrendResponse trend = trendMap.get(month);
            if (trend == null) {
                continue;
            }
            trend.setFeedbackCount(valueOrZero(projection.getFeedbackCount()));
            trend.setCompletedCount(valueOrZero(projection.getCompletedCount()));
            trend.setPm25ExceedCount(valueOrZero(projection.getPm25ExceedCount()));
            trend.setSo2ExceedCount(valueOrZero(projection.getSo2ExceedCount()));
            trend.setCoExceedCount(valueOrZero(projection.getCoExceedCount()));
        }

        return List.copyOf(trendMap.values());
    }

    private long valueOrZero(Long value) {
        return value == null ? 0 : value;
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
