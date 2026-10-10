package com.neusoft.supervision.statistics.dto;

public class ProvincePollutantStatResponse {
    private String provinceName;
    private String provinceShortName;
    private long pm25ExceedCount;
    private long so2ExceedCount;
    private long coExceedCount;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceShortName() {
        return provinceShortName;
    }

    public void setProvinceShortName(String provinceShortName) {
        this.provinceShortName = provinceShortName;
    }

    public long getPm25ExceedCount() {
        return pm25ExceedCount;
    }

    public void setPm25ExceedCount(long pm25ExceedCount) {
        this.pm25ExceedCount = pm25ExceedCount;
    }

    public long getSo2ExceedCount() {
        return so2ExceedCount;
    }

    public void setSo2ExceedCount(long so2ExceedCount) {
        this.so2ExceedCount = so2ExceedCount;
    }

    public long getCoExceedCount() {
        return coExceedCount;
    }

    public void setCoExceedCount(long coExceedCount) {
        this.coExceedCount = coExceedCount;
    }
}
