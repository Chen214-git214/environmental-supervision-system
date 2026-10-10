package com.neusoft.supervision.feedback.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GridTaskResponse {
    private Long afId;
    private Long feedbackId;
    private Long supervisorId;
    private String supervisorName;
    private String supervisorPhone;
    private Long cityId;
    private String cityName;
    private String estimatedLevel;
    private String pollutionType;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String description;
    private String imageUrl;
    private Integer status;
    private String statusText;
    private Long assignedGridMemberId;
    private String assignedGridMemberName;
    private LocalDateTime assignedAt;
    private LocalDateTime feedbackDate;
    private BigDecimal so2Value;
    private BigDecimal coValue;
    private BigDecimal pm25Value;
    private String so2Level;
    private String coLevel;
    private String pm25Level;
    private String aqiLevel;
    private Integer aqiValue;
    private LocalDateTime measuredAt;

    public Integer getAqiValue() { return aqiValue; }
    public void setAqiValue(Integer aqiValue) { this.aqiValue = aqiValue; }

    public Long getAfId() { return afId; }
    public void setAfId(Long afId) { this.afId = afId; }
    public Long getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Long feedbackId) { this.feedbackId = feedbackId; }
    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }
    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }
    public String getSupervisorPhone() { return supervisorPhone; }
    public void setSupervisorPhone(String supervisorPhone) { this.supervisorPhone = supervisorPhone; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getEstimatedLevel() { return estimatedLevel; }
    public void setEstimatedLevel(String estimatedLevel) { this.estimatedLevel = estimatedLevel; }
    public String getPollutionType() { return pollutionType; }
    public void setPollutionType(String pollutionType) { this.pollutionType = pollutionType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public Long getAssignedGridMemberId() { return assignedGridMemberId; }
    public void setAssignedGridMemberId(Long assignedGridMemberId) { this.assignedGridMemberId = assignedGridMemberId; }
    public String getAssignedGridMemberName() { return assignedGridMemberName; }
    public void setAssignedGridMemberName(String assignedGridMemberName) { this.assignedGridMemberName = assignedGridMemberName; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getFeedbackDate() { return feedbackDate; }
    public void setFeedbackDate(LocalDateTime feedbackDate) { this.feedbackDate = feedbackDate; }
    public BigDecimal getSo2Value() { return so2Value; }
    public void setSo2Value(BigDecimal so2Value) { this.so2Value = so2Value; }
    public BigDecimal getCoValue() { return coValue; }
    public void setCoValue(BigDecimal coValue) { this.coValue = coValue; }
    public BigDecimal getPm25Value() { return pm25Value; }
    public void setPm25Value(BigDecimal pm25Value) { this.pm25Value = pm25Value; }
    public String getSo2Level() { return so2Level; }
    public void setSo2Level(String so2Level) { this.so2Level = so2Level; }
    public String getCoLevel() { return coLevel; }
    public void setCoLevel(String coLevel) { this.coLevel = coLevel; }
    public String getPm25Level() { return pm25Level; }
    public void setPm25Level(String pm25Level) { this.pm25Level = pm25Level; }
    public String getAqiLevel() { return aqiLevel; }
    public void setAqiLevel(String aqiLevel) { this.aqiLevel = aqiLevel; }
    public LocalDateTime getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(LocalDateTime measuredAt) { this.measuredAt = measuredAt; }
}
