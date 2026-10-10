package com.neusoft.supervision.feedback.support;

/**
 * AQI 计算结果：三名单项级别 ID + 综合 AQI 数值。
 *
 * @param so2LevelId  SO2 单项级别 ID（对应 aqi_level.level_id）
 * @param coLevelId   CO 单项级别 ID
 * @param pm25LevelId PM2.5 单项级别 ID
 * @param aqiValue    综合 AQI 数值，为三者 IAQI 的最大值
 */
public record AqiEvaluation(long so2LevelId, long coLevelId, long pm25LevelId, int aqiValue) {

    /**
     * 综合 AQI 对应的级别 ID。
     */
    public long aqiLevelId() {
        return AqiCalculator.levelIdOf(aqiValue);
    }
}
