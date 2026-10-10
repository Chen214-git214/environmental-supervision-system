package com.neusoft.supervision.feedback.dto;

import java.time.LocalDateTime;

/**
 * 管理员处理反馈后的返回结果，对应接口文档《处理公众监督反馈》。
 */
public class FeedbackHandleResponse {
    private Long feedbackId;
    private Integer status;
    private String statusText;
    private String reply;
    private Long handlerAdminId;
    private String handlerName;
    private LocalDateTime handledAt;
    private LocalDateTime assignedAt;
    private LocalDateTime measuredAt;

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
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

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Long getHandlerAdminId() {
        return handlerAdminId;
    }

    public void setHandlerAdminId(Long handlerAdminId) {
        this.handlerAdminId = handlerAdminId;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }
}
