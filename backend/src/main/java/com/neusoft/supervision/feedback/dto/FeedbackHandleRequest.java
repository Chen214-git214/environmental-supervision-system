package com.neusoft.supervision.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 管理员处理反馈请求。
 *
 * <p>处理人身份由令牌解析，因此不再接收 handlerAdminId。</p>
 */
public class FeedbackHandleRequest {
    /** 处理后状态：2 已处理（办结）、3 已驳回 */
    @NotNull(message = "处理后状态不能为空")
    private Integer status;

    @NotBlank(message = "处理回复不能为空")
    @Size(max = 500, message = "处理回复长度不能超过 500")
    private String reply;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
