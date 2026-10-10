package com.neusoft.supervision.feedback.support;

/**
 * 字典种子数据。
 *
 * <p>省、市、AQI 级别三张字典表既有初始数据，运行时也供 {@code DictionaryService} 反查名称与跨省判定。
 * AQI 分级区间按现行标准写入 <b>数据库</b>，避免再出现"阈值硬编码在前端"的问题。</p>
 */
public final class DictionarySeed {
    /** 省级行政区划编码前缀 + 6 个 0 */
    public record ProvinceSeed(Long provinceId, String provinceCode, String provinceName, int sortNo) {
    }

    public record CitySeed(Long cityId, Long provinceId, String cityCode, String cityName, int sortNo) {
    }

    public record AqiLevelSeed(Long levelId, String levelName, int minAqi, int maxAqi, String healthEffect, String suggestion) {
    }

    public static final Long PROVINCE_LIAONING = 1L;
    public static final Long PROVINCE_BEIJING = 2L;
    public static final Long PROVINCE_TIANJIN = 3L;

    private DictionarySeed() {
    }

    public static ProvinceSeed[] provinces() {
        return new ProvinceSeed[] {
                new ProvinceSeed(PROVINCE_LIAONING, "210000", "辽宁省", 1),
                new ProvinceSeed(PROVINCE_BEIJING, "110000", "北京市", 2),
                new ProvinceSeed(PROVINCE_TIANJIN, "120000", "天津市", 3)
        };
    }

    public static CitySeed[] cities() {
        return new CitySeed[] {
                new CitySeed(1L, PROVINCE_LIAONING, "210100", "沈阳市", 1),
                new CitySeed(2L, PROVINCE_LIAONING, "210200", "大连市", 2),
                new CitySeed(3L, PROVINCE_LIAONING, "210300", "鞍山市", 3),
                new CitySeed(4L, PROVINCE_BEIJING, "110100", "北京市", 4),
                new CitySeed(5L, PROVINCE_TIANJIN, "120100", "天津市", 5)
        };
    }

    public static AqiLevelSeed[] aqiLevels() {
        return new AqiLevelSeed[] {
                new AqiLevelSeed(1L, "优", 0, 50,
                        "空气质量令人满意，基本无空气污染。", "各类人群可正常活动。"),
                new AqiLevelSeed(2L, "良", 51, 100,
                        "空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响。",
                        "极少数异常敏感人群应减少户外活动。"),
                new AqiLevelSeed(3L, "轻度污染", 101, 150,
                        "易感人群症状有轻度加剧，健康人群出现刺激症状。",
                        "儿童、老年人及心脏病、呼吸系统疾病患者应减少长时间、高强度的户外锻炼。"),
                new AqiLevelSeed(4L, "中度污染", 151, 200,
                        "进一步加剧易感人群症状，可能对健康人群心脏、呼吸系统有影响。",
                        "儿童、老年人及心脏病、呼吸系统疾病患者避免长时间、高强度的户外锻炼，一般人群适量减少户外运动。"),
                new AqiLevelSeed(5L, "重度污染", 201, 300,
                        "心脏病和肺病患者症状显著加剧，运动耐受力降低，健康人群普遍出现症状。",
                        "儿童、老年人和心脏病、肺病患者应停留在室内，停止户外运动，一般人群减少户外运动。"),
                new AqiLevelSeed(6L, "严重污染", 301, 500,
                        "健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病。",
                        "老年人和病人应停留在室内，避免体力消耗，一般人群应避免户外活动。")
        };
    }
}
