package com.neusoft.supervision.feedback.dto;

import java.time.LocalDateTime;

/**
 * 公众监督员查看自己提交的反馈（含处理进展，但不含监督员个人联系方式等管理端字段）。
 */
public class MyFeedbackResponse {
    private Long feedbackId;
    private Long cityId;
    private String cityName;
    private String pollutionType;
    private String address;
    private String description;
    private Integer status;
    private String statusText;
    private String assignedGridMemberName;
    private LocalDateTime submittedAt;
    private String so2Level;
    private String coLevel;
    private String pm25Level;
    private String aqiLevel;
    private Integer aqiValue;
    private LocalDateTime measuredAt;

    /** 管理员处理回复与处理时间：公众监督员据此看到处理结果 */
    private String replyContent;
    private LocalDateTime handledAt;

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public Integer getAqiValue() {
        return aqiValue;
    }

    public void setAqiValue(Integer aqiValue) {
        this.aqiValue = aqiValue;
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

    public String getPollutionType() {
        return pollutionType;
    }

    public void setPollutionType(String pollutionType) {
        this.pollutionType = pollutionType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getAssignedGridMemberName() {
        return assignedGridMemberName;
    }

    public void setAssignedGridMemberName(String assignedGridMemberName) {
        this.assignedGridMemberName = assignedGridMemberName;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getSo2Level() {
        return so2Level;
    }

    public void setSo2Level(String so2Level) {
        this.so2Level = so2Level;
    }

    public String getCoLevel() {
        return coLevel;
    }

    public void setCoLevel(String coLevel) {
        this.coLevel = coLevel;
    }

    public String getPm25Level() {
        return pm25Level;
    }

    public void setPm25Level(String pm25Level) {
        this.pm25Level = pm25Level;
    }

    public String getAqiLevel() {
        return aqiLevel;
    }

    public void setAqiLevel(String aqiLevel) {
        this.aqiLevel = aqiLevel;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }
}
