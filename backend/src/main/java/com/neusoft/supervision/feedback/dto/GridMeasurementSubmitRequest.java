package com.neusoft.supervision.feedback.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 网格员提交实测数据。
 *
 * <p>只接收三个污染物浓度值。单项等级与 AQI 一律由服务端计算：
 * 此前客户端可以直接提交 {@code aqiLevel}，等于把判定权交给了上报方，统计结果无法信任。</p>
 *
 * <p>也不含网格员编号：上报人身份由 JWT 解析得到，避免冒用他人身份提交。</p>
 *
 * <p>浓度单位：SO2、PM2.5 为 μg/m³，CO 为 mg/m³。</p>
 */
public class GridMeasurementSubmitRequest {
    @NotNull(message = "SO2 实测值不能为空")
    @DecimalMin(value = "0.0", message = "SO2 实测值不能小于 0")
    private BigDecimal so2Value;

    @NotNull(message = "CO 实测值不能为空")
    @DecimalMin(value = "0.0", message = "CO 实测值不能小于 0")
    private BigDecimal coValue;

    @NotNull(message = "PM2.5 实测值不能为空")
    @DecimalMin(value = "0.0", message = "PM2.5 实测值不能小于 0")
    private BigDecimal pm25Value;

    /** 以下三项按设计书存库，当前不参与 AQI 计算（AQI 取 SO2/CO/PM2.5） */
    @DecimalMin(value = "0.0", message = "PM10 实测值不能小于 0")
    private BigDecimal pm10Value;

    @DecimalMin(value = "0.0", message = "NO2 实测值不能小于 0")
    private BigDecimal no2Value;

    @DecimalMin(value = "0.0", message = "O3 实测值不能小于 0")
    private BigDecimal o3Value;

    public BigDecimal getPm10Value() {
        return pm10Value;
    }

    public void setPm10Value(BigDecimal pm10Value) {
        this.pm10Value = pm10Value;
    }

    public BigDecimal getNo2Value() {
        return no2Value;
    }

    public void setNo2Value(BigDecimal no2Value) {
        this.no2Value = no2Value;
    }

    public BigDecimal getO3Value() {
        return o3Value;
    }

    public void setO3Value(BigDecimal o3Value) {
        this.o3Value = o3Value;
    }

    public BigDecimal getSo2Value() {
        return so2Value;
    }

    public void setSo2Value(BigDecimal so2Value) {
        this.so2Value = so2Value;
    }

    public BigDecimal getCoValue() {
        return coValue;
    }

    public void setCoValue(BigDecimal coValue) {
        this.coValue = coValue;
    }

    public BigDecimal getPm25Value() {
        return pm25Value;
    }

    public void setPm25Value(BigDecimal pm25Value) {
        this.pm25Value = pm25Value;
    }
}
