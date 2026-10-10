package com.neusoft.supervision.auth.dto;

public class AdminLoginResponse {
    private String token;
    private AdminInfo adminInfo;

    public AdminLoginResponse() {
    }

    public AdminLoginResponse(String token, AdminInfo adminInfo) {
        this.token = token;
        this.adminInfo = adminInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AdminInfo getAdminInfo() {
        return adminInfo;
    }

    public void setAdminInfo(AdminInfo adminInfo) {
        this.adminInfo = adminInfo;
    }
}
