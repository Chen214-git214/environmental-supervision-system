package com.neusoft.supervision.feedback.dto;

/**
 * 公众监督员信息（不含密码）。
 */
public class PublicSupervisorInfo {
    private Long supervisorId;
    private String username;
    private String realName;
    private String phone;
    private Integer status;

    public PublicSupervisorInfo() {
    }

    public PublicSupervisorInfo(Long supervisorId, String username, String realName, String phone, Integer status) {
        this.supervisorId = supervisorId;
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.status = status;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
