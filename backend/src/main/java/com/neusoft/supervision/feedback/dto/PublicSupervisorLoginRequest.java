package com.neusoft.supervision.feedback.dto;

import jakarta.validation.constraints.NotBlank;

public class PublicSupervisorLoginRequest {
    @NotBlank(message = "登录账号不能为空")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
