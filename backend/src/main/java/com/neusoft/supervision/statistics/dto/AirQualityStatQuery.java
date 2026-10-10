package com.neusoft.supervision.statistics.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 监测统计查询条件。三个维度均可选，时间范围格式与接口文档一致（yyyy-MM-dd HH:mm:ss）。
 */
public class AirQualityStatQuery {
    private Long cityId;
    private Long gridMemberId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
