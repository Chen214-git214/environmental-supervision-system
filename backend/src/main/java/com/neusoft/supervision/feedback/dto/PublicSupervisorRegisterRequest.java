package com.neusoft.supervision.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 公众监督员注册请求。字段与接口文档《公众监督员注册》一致。
 */
public class PublicSupervisorRegisterRequest {
    @NotBlank(message = "登录账号不能为空")
    @Size(min = 4, max = 50, message = "登录账号长度需在 4-50 之间")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    @Size(min = 6, max = 32, message = "登录密码长度需在 6-32 之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过 50")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(
            regexp = "^$|(^\\d{17}[\\dXx]$)|(^\\d{15}$)",
            message = "身份证号格式不正确")
    private String idCard;

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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
}
