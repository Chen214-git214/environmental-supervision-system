package com.neusoft.supervision.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "grid_member")
public class GridMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grid_member_id")
    private Long gridMemberId;

    @Column(name = "employee_no", nullable = false, length = 30)
    private String employeeNo;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "province_id", nullable = false)
    private Long provinceId;

    @Column(name = "province_name", nullable = false, length = 50)
    private String provinceName;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(name = "city_name", nullable = false, length = 50)
    private String cityName;

    @Column(name = "grid_name", nullable = false, length = 80)
    private String gridName;

    @Column(nullable = false)
    private Integer status;

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

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
