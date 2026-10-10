package com.neusoft.supervision.feedback.dto;

/**
 * 公众监督员注册/登录响应：返回令牌以便注册或登录后直接进入反馈流程。
 */
public class PublicSupervisorAuthResponse {
    private String token;
    private PublicSupervisorInfo supervisorInfo;

    public PublicSupervisorAuthResponse() {
    }

    public PublicSupervisorAuthResponse(String token, PublicSupervisorInfo supervisorInfo) {
        this.token = token;
        this.supervisorInfo = supervisorInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PublicSupervisorInfo getSupervisorInfo() {
        return supervisorInfo;
    }

    public void setSupervisorInfo(PublicSupervisorInfo supervisorInfo) {
        this.supervisorInfo = supervisorInfo;
    }
}
