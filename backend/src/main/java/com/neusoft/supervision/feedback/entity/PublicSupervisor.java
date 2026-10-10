package com.neusoft.supervision.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 公众监督员账号。对应数据库设计中的 public_supervisor 表。
 */
@Entity
@Table(name = "public_supervisor")
public class PublicSupervisor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supervisor_id")
    private Long supervisorId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 存储 BCrypt 摘要，不存明文 */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "id_card", length = 30)
    private String idCard;

    /** 1 启用、0 禁用 */
    @Column(nullable = false)
    private Integer status;

    @Column(name = "register_time", nullable = false)
    private LocalDateTime registerTime;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }
}
