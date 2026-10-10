package com.neusoft.supervision.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "public_feedback")
public class PublicFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "supervisor_id", nullable = false)
    private Long supervisorId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(name = "level_id")
    private Long levelId;

    @Column(name = "pollution_type", nullable = false, length = 30)
    private String pollutionType;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "assigned_grid_member_id")
    private Long assignedGridMemberId;

    @Column(name = "assigned_grid_member_name", length = 50)
    private String assignedGridMemberName;

    @Column(name = "assignment_type", length = 20)
    private String assignmentType;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "so2_value", precision = 10, scale = 2)
    private BigDecimal so2Value;

    @Column(name = "co_value", precision = 10, scale = 2)
    private BigDecimal coValue;

    @Column(name = "pm25_value", precision = 10, scale = 2)
    private BigDecimal pm25Value;

    @Column(name = "so2_level", length = 20)
    private String so2Level;

    @Column(name = "co_level", length = 20)
    private String coLevel;

    @Column(name = "pm25_level", length = 20)
    private String pm25Level;

    @Column(name = "aqi_level", length = 20)
    private String aqiLevel;

    /** AQI 数值，取各污染物 IAQI 的最大值，由服务端计算得出 */
    @Column(name = "aqi_value")
    private Integer aqiValue;

    @Column(name = "measured_at")
    private LocalDateTime measuredAt;

    /** 处理人（管理员）编号 */
    @Column(name = "admin_id")
    private Long adminId;

    /** 管理员处理回复 */
    @Column(name = "reply_content", length = 500)
    private String replyContent;

    /** 处理时间 */
    @Column(name = "handle_time")
    private LocalDateTime handledAt;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
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

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getAssignedGridMemberId() {
        return assignedGridMemberId;
    }

    public void setAssignedGridMemberId(Long assignedGridMemberId) {
        this.assignedGridMemberId = assignedGridMemberId;
    }

    public String getAssignedGridMemberName() {
        return assignedGridMemberName;
    }

    public void setAssignedGridMemberName(String assignedGridMemberName) {
        this.assignedGridMemberName = assignedGridMemberName;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public BigDecimal getSo2Value() {
        return so2Value;
    }

    public void setSo2Value(BigDecimal so2Value) {
        this.so2Value = so2Value;
    }

    public BigDecimal getCoValue() {
        return coValue;
    }

    public void setCoValue(BigDecimal coValue) {
        this.coValue = coValue;
    }

    public BigDecimal getPm25Value() {
        return pm25Value;
    }

    public void setPm25Value(BigDecimal pm25Value) {
        this.pm25Value = pm25Value;
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

    public Integer getAqiValue() {
        return aqiValue;
    }

    public void setAqiValue(Integer aqiValue) {
        this.aqiValue = aqiValue;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
