package com.neusoft.supervision.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 空气质量实测数据（air_quality_stat）查询结果，对应接口文档《查询空气质量监测数据统计》。
 */
public class AirQualityStatResponse {
    private Long statId;
    private Long feedbackId;
    private Long cityId;
    private String cityName;
    private Long gridMemberId;
    private String gridMemberName;
    private Long levelId;
    private String levelName;
    private Integer aqi;
    private BigDecimal pm25;
    private BigDecimal pm10;
    private BigDecimal so2;
    private BigDecimal no2;
    private BigDecimal co;
    private BigDecimal o3;
    private LocalDateTime monitoredAt;

    public AirQualityStatResponse() {
    }

    /**
     * 供 JPQL 构造表达式使用，参数顺序必须与查询中的 new 表达式一致。
     */
    public AirQualityStatResponse(
            Long statId,
            Long feedbackId,
            Long cityId,
            String cityName,
            Long gridMemberId,
            String gridMemberName,
            Long levelId,
            String levelName,
            Integer aqi,
            BigDecimal pm25,
            BigDecimal pm10,
            BigDecimal so2,
            BigDecimal no2,
            BigDecimal co,
            BigDecimal o3,
            LocalDateTime monitoredAt) {
        this.statId = statId;
        this.feedbackId = feedbackId;
        this.cityId = cityId;
        this.cityName = cityName;
        this.gridMemberId = gridMemberId;
        this.gridMemberName = gridMemberName;
        this.levelId = levelId;
        this.levelName = levelName;
        this.aqi = aqi;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.so2 = so2;
        this.no2 = no2;
        this.co = co;
        this.o3 = o3;
        this.monitoredAt = monitoredAt;
    }

    public Long getStatId() {
        return statId;
    }

    public void setStatId(Long statId) {
        this.statId = statId;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Long getGridMemberId() {
        return gridMemberId;
    }

    public void setGridMemberId(Long gridMemberId) {
        this.gridMemberId = gridMemberId;
    }

    public String getGridMemberName() {
        return gridMemberName;
    }

    public void setGridMemberName(String gridMemberName) {
        this.gridMemberName = gridMemberName;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public BigDecimal getPm25() {
        return pm25;
    }

    public void setPm25(BigDecimal pm25) {
        this.pm25 = pm25;
    }

    public BigDecimal getPm10() {
        return pm10;
    }

    public void setPm10(BigDecimal pm10) {
        this.pm10 = pm10;
    }

    public BigDecimal getSo2() {
        return so2;
    }

    public void setSo2(BigDecimal so2) {
        this.so2 = so2;
    }

    public BigDecimal getNo2() {
        return no2;
    }

    public void setNo2(BigDecimal no2) {
        this.no2 = no2;
    }

    public BigDecimal getCo() {
        return co;
    }

    public void setCo(BigDecimal co) {
        this.co = co;
    }

    public BigDecimal getO3() {
        return o3;
    }

    public void setO3(BigDecimal o3) {
        this.o3 = o3;
    }

    public LocalDateTime getMonitoredAt() {
        return monitoredAt;
    }

    public void setMonitoredAt(LocalDateTime monitoredAt) {
        this.monitoredAt = monitoredAt;
    }
}
