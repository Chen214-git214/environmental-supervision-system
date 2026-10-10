package com.neusoft.supervision.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminLoginRequest {
    @NotBlank(message = "不能为空")
    private String adminCode;

    @NotBlank(message = "不能为空")
    private String password;

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
