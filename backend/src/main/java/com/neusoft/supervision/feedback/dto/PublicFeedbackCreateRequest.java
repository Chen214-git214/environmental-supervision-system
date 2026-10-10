package com.neusoft.supervision.feedback.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 公众监督员提交反馈。
 *
 * <p>不含 supervisorId：提交人身份由 JWT 解析得到，避免请求方以他人身份提交反馈。</p>
 */
public class PublicFeedbackCreateRequest {
    @NotNull
    private Long cityId;

    private Long levelId;

    @NotBlank
    @Size(max = 30)
    private String pollutionType;

    @NotBlank
    @Size(max = 200)
    private String address;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitude;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitude;

    @NotBlank
    private String description;

    @Size(max = 255)
    private String imageUrl;

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
}
