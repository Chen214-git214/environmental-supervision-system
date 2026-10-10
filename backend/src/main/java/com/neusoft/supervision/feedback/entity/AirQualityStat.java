package com.neusoft.supervision.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 空气质量监测数据表（air_quality_stat）。对应数据库设计第 7 张表。
 *
 * <p>网格员每次提交实测数据都会新增一条记录，与反馈构成"1 条反馈 : N 条监测数据"的关系
 * （数据库设计中的 {@code grid_member 1:N air_quality_stat}、{@code aqi_level 1:N air_quality_stat}）。
 * 此前实测数据直接存在 public_feedback 的列上，一条反馈只能留一次测量结果。</p>
 */
@Entity
@Table(name = "air_quality_stat")
public class AirQualityStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId;

    /** 所属反馈，一条反馈可对应多次实测 */
    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    /** 上报人（网格员） */
    @Column(name = "grid_member_id", nullable = false)
    private Long gridMemberId;

    /** 该条监测数据所属的 AQI 级别 */
    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @Column(name = "aqi_value", nullable = false)
    private Integer aqiValue;

    @Column(precision = 8, scale = 2)
    private BigDecimal pm25;

    @Column(precision = 8, scale = 2)
    private BigDecimal pm10;

    @Column(precision = 8, scale = 2)
    private BigDecimal so2;

    @Column(precision = 8, scale = 2)
    private BigDecimal no2;

    @Column(precision = 8, scale = 2)
    private BigDecimal co;

    @Column(precision = 8, scale = 2)
    private BigDecimal o3;

    /** 监测时间 */
    @Column(name = "monitor_time", nullable = false)
    private LocalDateTime monitorTime;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

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

    public Long getGridMemberId() {
        return gridMemberId;
    }

    public void setGridMemberId(Long gridMemberId) {
        this.gridMemberId = gridMemberId;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public Integer getAqiValue() {
        return aqiValue;
    }

    public void setAqiValue(Integer aqiValue) {
        this.aqiValue = aqiValue;
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

    public LocalDateTime getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(LocalDateTime monitorTime) {
        this.monitorTime = monitorTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
