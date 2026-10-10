package com.neusoft.supervision.feedback.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * AQI 计算器。
 *
 * <p>按各污染物的浓度限值分段，用线性插值求出单项 IAQI，再取三者最大值作为 AQI，
 * 即 {@code AQI = max(IAQI_SO2, IAQI_CO, IAQI_PM25)}。</p>
 *
 * <p>此前这段计算放在前端的 {@code GridTaskDetailView} 里，且只是"取三个等级标签的最大值"——
 * 既没有算出 AQI 数值，客户端还能任意伪造等级。现在统一由服务端计算并落库。</p>
 *
 * <p>浓度单位：SO2、PM2.5 为 μg/m³，CO 为 mg/m³。</p>
 */
public final class AqiCalculator {
    /** IAQI 分级区间：每个污染物等级跨越 50 个 IAQI */
    private static final int IAQI_BAND = 50;

    /** 各污染物浓度限值，下标 0~4 依次对应 IAQI 0-50、51-100、101-150、151-200、201-300，超过最后一段按 500 计 */
    private static final double[] SO2_LIMITS = {50, 150, 475, 800, 1600};
    private static final double[] CO_LIMITS = {2, 4, 14, 24, 36};
    private static final double[] PM25_LIMITS = {35, 75, 115, 150, 250};

    /** 浓度合理上限，超出视为录入错误（单位同各限值） */
    private static final double SO2_MAX = 3000;
    private static final double CO_MAX = 60;
    private static final double PM25_MAX = 1000;

    private AqiCalculator() {
    }

    public static AqiEvaluation evaluate(BigDecimal so2, BigDecimal co, BigDecimal pm25) {
        double so2Value = require(so2, "SO2", SO2_MAX);
        double coValue = require(co, "CO", CO_MAX);
        double pm25Value = require(pm25, "PM2.5", PM25_MAX);

        int so2Iaqi = iaqiOf(so2Value, SO2_LIMITS);
        int coIaqi = iaqiOf(coValue, CO_LIMITS);
        int pm25Iaqi = iaqiOf(pm25Value, PM25_LIMITS);

        return new AqiEvaluation(
                levelIdOf(so2Iaqi),
                levelIdOf(coIaqi),
                levelIdOf(pm25Iaqi),
                Math.max(so2Iaqi, Math.max(coIaqi, pm25Iaqi)));
    }

    /**
     * 单项 IAQI：定位浓度所处的分段，在该段的浓度区间与 IAQI 区间之间线性插值。
     */
    static int iaqiOf(double concentration, double[] limits) {
        int index = bandIndexOf(concentration, limits);

        if (index >= limits.length) {
            // 超过最高一段（IAQI 201-300）时按 500 封顶，与现行标准的处理一致
            return 500;
        }

        double lowerLimit = index == 0 ? 0 : limits[index - 1];
        int lowerIaqi = index * IAQI_BAND;
        double ratio = (concentration - lowerLimit) / (limits[index] - lowerLimit);

        return (int) Math.round(lowerIaqi + ratio * IAQI_BAND);
    }

    /**
     * 浓度所处的分段下标：0 表示未超过第一个限值，依次类推；等于 limits.length 表示超过最后一个限值。
     */
    static int bandIndexOf(double concentration, double[] limits) {
        int position = Arrays.binarySearch(limits, concentration);

        if (position >= 0) {
            // 正好等于某个限值：归属到该限值所在分段的上一段边界，例如 50 应落在 IAQI 50
            return position;
        }

        return Math.min(-position - 1, limits.length);
    }

    /**
     * 单项 IAQI 值换算为 aqi_level 表的级别 ID。
     *
     * <p>级别区间的唯一口径是 aqi_level 表的 min_aqi / max_aqi：0-50→1、51-100→2、101-150→3、
     * 151-200→4、201-300→5、301-500→6。因第五段宽 100（其余段宽 50），换算时必须先处理该段，
     * 否则 IAQI 201-250 会被错判为 5 级之外的级别。</p>
     */
    static long levelIdOf(int iaqi) {
        if (iaqi <= 50) {
            return 1;
        }

        if (iaqi <= 100) {
            return 2;
        }

        if (iaqi <= 150) {
            return 3;
        }

        if (iaqi <= 200) {
            return 4;
        }

        // 201-300 为 5 级，301 以上为 6 级
        return iaqi <= 300 ? 5 : 6;
    }

    private static double require(BigDecimal value, String name, double max) {
        if (value == null) {
            throw new IllegalArgumentException(name + " 实测值不能为空");
        }

        double numeric = value.setScale(2, RoundingMode.HALF_UP).doubleValue();

        if (numeric < 0) {
            throw new IllegalArgumentException(name + " 实测值不能小于 0");
        }

        if (numeric > max) {
            throw new IllegalArgumentException(name + " 实测值超出合理范围（上限 " + (int) max + "）");
        }

        return numeric;
    }
}
