package com.neusoft.supervision.auth.dto;

public class GridMemberInfo {
    private Long gridMemberId;
    private String employeeNo;
    private String realName;
    private String phone;
    private String provinceName;
    private String cityName;
    private String gridName;

    public GridMemberInfo() {
    }

    public GridMemberInfo(Long gridMemberId, String employeeNo, String realName, String phone, String provinceName, String cityName, String gridName) {
        this.gridMemberId = gridMemberId;
        this.employeeNo = employeeNo;
        this.realName = realName;
        this.phone = phone;
        this.provinceName = provinceName;
        this.cityName = cityName;
        this.gridName = gridName;
    }

    public Long getGridMemberId() {
        return gridMemberId;
    }

    public void setGridMemberId(Long gridMemberId) {
        this.gridMemberId = gridMemberId;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
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

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getGridName() {
        return gridName;
    }

    public void setGridName(String gridName) {
        this.gridName = gridName;
    }
}
