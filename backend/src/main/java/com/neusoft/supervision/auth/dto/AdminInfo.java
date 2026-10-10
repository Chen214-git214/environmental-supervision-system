package com.neusoft.supervision.auth.dto;

public class AdminInfo {
    private Long id;
    private String adminCode;
    private String name;

    public AdminInfo() {
    }

    public AdminInfo(Long id, String adminCode, String name) {
        this.id = id;
        this.adminCode = adminCode;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
