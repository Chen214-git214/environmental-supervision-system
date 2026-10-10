package com.neusoft.supervision.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class GridMemberLoginRequest {
    @NotBlank(message = "网格员账号不能为空")
    private String employeeNo;

    @NotBlank(message = "密码不能为空")
    private String password;

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
