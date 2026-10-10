package com.neusoft.supervision.feedback.support;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AQI 计算的边界验证。阈值与 aqi_level 字典表一致（SO2 24h / CO 24h / PM2.5 24h）。
 */
class AqiCalculatorTest {

    private static final double[] SO2_LIMITS = {50, 150, 475, 800, 1600};
    private static final double[] CO_LIMITS = {2, 4, 14, 24, 36};
    private static final double[] PM25_LIMITS = {35, 75, 115, 150, 250};

    @Test
    void iaqiAtSegmentBoundaries() {
        // 浓度恰好等于某个限值时，落在该段的 IAQI 起点：
        // 35→50、75→100、115→150、150→200、250→250（250 是最后一段的起点）
        assertEquals(0, AqiCalculator.iaqiOf(0, PM25_LIMITS));
        assertEquals(50, AqiCalculator.iaqiOf(35, PM25_LIMITS));
        assertEquals(100, AqiCalculator.iaqiOf(75, PM25_LIMITS));
        assertEquals(150, AqiCalculator.iaqiOf(115, PM25_LIMITS));
        assertEquals(200, AqiCalculator.iaqiOf(150, PM25_LIMITS));
        assertEquals(250, AqiCalculator.iaqiOf(250, PM25_LIMITS));
    }

    @Test
    void iaqiIsLinearlyInterpolatedWithinSegment() {
        // 75 → 100 与 115 → 150 的中点应为 125
        assertEquals(125, AqiCalculator.iaqiOf(95, PM25_LIMITS));
        // 35 → 50 与 75 → 100 的中点应为 75
        assertEquals(75, AqiCalculator.iaqiOf(55, PM25_LIMITS));
        // CO：0 → 0 与 2 → 50 的中点应为 25
        assertEquals(25, AqiCalculator.iaqiOf(1, CO_LIMITS));
        // SO2：50 → 50 与 150 → 100 的中点应为 75
        assertEquals(75, AqiCalculator.iaqiOf(100, SO2_LIMITS));
    }

    @Test
    void iaqiCapsAtFiveHundredBeyondLastLimit() {
        assertEquals(500, AqiCalculator.iaqiOf(251, PM25_LIMITS));
        assertEquals(500, AqiCalculator.iaqiOf(9999, PM25_LIMITS));
        assertEquals(500, AqiCalculator.iaqiOf(1601, SO2_LIMITS));
        assertEquals(500, AqiCalculator.iaqiOf(36.1, CO_LIMITS));
    }

    @Test
    void levelIdFollowsDictionaryRanges() {
        // 区间口径与 aqi_level 表的 min_aqi / max_aqi 一致
        assertEquals(1, AqiCalculator.levelIdOf(0));
        assertEquals(1, AqiCalculator.levelIdOf(50));
        assertEquals(2, AqiCalculator.levelIdOf(51));
        assertEquals(2, AqiCalculator.levelIdOf(100));
        assertEquals(3, AqiCalculator.levelIdOf(101));
        assertEquals(3, AqiCalculator.levelIdOf(150));
        assertEquals(4, AqiCalculator.levelIdOf(151));
        assertEquals(4, AqiCalculator.levelIdOf(200));
        assertEquals(5, AqiCalculator.levelIdOf(201));
        // 第五段宽 100（201-300），不能按等宽 50 推算
        assertEquals(5, AqiCalculator.levelIdOf(260));
        assertEquals(5, AqiCalculator.levelIdOf(300));
        assertEquals(6, AqiCalculator.levelIdOf(301));
        assertEquals(6, AqiCalculator.levelIdOf(500));
    }

    @Test
    void aqiTakesMaximumOfThreePollutants() {
        // SO2=100 → 75，CO=1 → 25，PM2.5=95 → 125 ⇒ AQI 取 125（轻度污染）
        AqiEvaluation evaluation = AqiCalculator.evaluate(
                new BigDecimal("100"), new BigDecimal("1"), new BigDecimal("95"));

        assertEquals(75, AqiCalculator.iaqiOf(100, SO2_LIMITS));
        assertEquals(125, evaluation.aqiValue());
        assertEquals(3, evaluation.aqiLevelId());
        assertEquals(2, evaluation.so2LevelId());
        assertEquals(1, evaluation.coLevelId());
        assertEquals(3, evaluation.pm25LevelId());
    }

    @Test
    void allZeroConcentrationsProduceAqiZero() {
        AqiEvaluation evaluation = AqiCalculator.evaluate(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        assertEquals(0, evaluation.aqiValue());
        assertEquals(1, evaluation.aqiLevelId());
        assertEquals(1, evaluation.so2LevelId());
        assertEquals(1, evaluation.coLevelId());
        assertEquals(1, evaluation.pm25LevelId());
    }

    @Test
    void extremeConcentrationsProduceHighestLevel() {
        AqiEvaluation evaluation = AqiCalculator.evaluate(
                new BigDecimal("2000"), new BigDecimal("40"), new BigDecimal("400"));

        assertEquals(500, evaluation.aqiValue());
        assertEquals(6, evaluation.aqiLevelId());
    }

    @Test
    void implausibleConcentrationsAreRejected() {
        IllegalArgumentException so2 = assertThrows(IllegalArgumentException.class, () ->
                AqiCalculator.evaluate(new BigDecimal("3001"), BigDecimal.ONE, BigDecimal.ONE));
        assertTrue(so2.getMessage().contains("SO2"));

        IllegalArgumentException co = assertThrows(IllegalArgumentException.class, () ->
                AqiCalculator.evaluate(BigDecimal.ONE, new BigDecimal("61"), BigDecimal.ONE));
        assertTrue(co.getMessage().contains("CO"));

        IllegalArgumentException pm25 = assertThrows(IllegalArgumentException.class, () ->
                AqiCalculator.evaluate(BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("1001")));
        assertTrue(pm25.getMessage().contains("PM2.5"));
    }

    @Test
    void negativeConcentrationsAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                AqiCalculator.evaluate(new BigDecimal("-1"), BigDecimal.ONE, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () ->
                AqiCalculator.evaluate(null, BigDecimal.ONE, BigDecimal.ONE));
    }

    /**
     * 与前端原本的阈值口径对齐：改造前后同一组浓度的定级结论必须一致，避免行为漂移。
     */
    @Test
    void matchesFrontendThresholdsPreviouslyUsed() {
        // 前端：SO2 [50,150,475,800,1600]、CO [2,4,14,24,36]、PM2.5 [35,75,115,150,250]
        assertEquals(2, AqiCalculator.levelIdOf(AqiCalculator.iaqiOf(60, SO2_LIMITS)));
        assertEquals(2, AqiCalculator.levelIdOf(AqiCalculator.iaqiOf(3.5, CO_LIMITS)));
        assertEquals(3, AqiCalculator.levelIdOf(AqiCalculator.iaqiOf(82, PM25_LIMITS)));
    }
}
