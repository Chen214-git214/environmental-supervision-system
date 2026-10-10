package com.neusoft.supervision.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 空气质量指数级别表（aqi_level）。级别判定基准，同时供反馈的"初步判定级别"引用。
 */
@Entity
@Table(name = "aqi_level")
public class AqiLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Long levelId;

    @Column(name = "level_name", nullable = false, length = 20)
    private String levelName;

    @Column(name = "min_aqi", nullable = false)
    private Integer minAqi;

    @Column(name = "max_aqi", nullable = false)
    private Integer maxAqi;

    @Column(name = "health_effect", length = 255)
    private String healthEffect;

    @Column(length = 255)
    private String suggestion;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

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

    public Integer getMinAqi() {
        return minAqi;
    }

    public void setMinAqi(Integer minAqi) {
        this.minAqi = minAqi;
    }

    public Integer getMaxAqi() {
        return maxAqi;
    }

    public void setMaxAqi(Integer maxAqi) {
        this.maxAqi = maxAqi;
    }

    public String getHealthEffect() {
        return healthEffect;
    }

    public void setHealthEffect(String healthEffect) {
        this.healthEffect = healthEffect;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
