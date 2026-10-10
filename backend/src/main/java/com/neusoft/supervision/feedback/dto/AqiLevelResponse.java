package com.neusoft.supervision.feedback.dto;

/**
 * AQI 级别字典项，供前端渲染级别下拉与健康提示。
 */
public class AqiLevelResponse {
    private Long levelId;
    private String levelName;
    private Integer minAqi;
    private Integer maxAqi;
    private String healthEffect;
    private String suggestion;

    public AqiLevelResponse() {
    }

    public AqiLevelResponse(Long levelId, String levelName, Integer minAqi, Integer maxAqi, String healthEffect, String suggestion) {
        this.levelId = levelId;
        this.levelName = levelName;
        this.minAqi = minAqi;
        this.maxAqi = maxAqi;
        this.healthEffect = healthEffect;
        this.suggestion = suggestion;
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
}
