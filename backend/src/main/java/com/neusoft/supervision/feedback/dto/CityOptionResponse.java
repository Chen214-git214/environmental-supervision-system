package com.neusoft.supervision.feedback.dto;

/**
 * 城市选项，含所属省名称，供前端渲染联动下拉。
 */
public class CityOptionResponse {
    private Long cityId;
    private Long provinceId;
    private String cityCode;
    private String cityName;
    private String provinceName;
    private Integer status;

    public CityOptionResponse() {
    }

    public CityOptionResponse(Long cityId, Long provinceId, String cityCode, String cityName, String provinceName, Integer status) {
        this.cityId = cityId;
        this.provinceId = provinceId;
        this.cityCode = cityCode;
        this.cityName = cityName;
        this.provinceName = provinceName;
        this.status = status;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
