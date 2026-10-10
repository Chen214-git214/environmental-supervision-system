package com.neusoft.supervision.feedback.service;

import com.neusoft.supervision.feedback.dto.AqiLevelResponse;
import com.neusoft.supervision.feedback.dto.CityOptionResponse;
import com.neusoft.supervision.feedback.entity.AqiLevel;
import com.neusoft.supervision.feedback.entity.GridCity;
import com.neusoft.supervision.feedback.entity.GridProvince;
import com.neusoft.supervision.feedback.repository.AqiLevelRepository;
import com.neusoft.supervision.feedback.repository.GridCityRepository;
import com.neusoft.supervision.feedback.repository.GridProvinceRepository;
import com.neusoft.supervision.feedback.support.StatusFlags;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 区域与级别字典。
 *
 * <p>省、市、AQI 级别这三张字典表数据量极小且几乎不变，因此加载进内存并缓存，
 * 供反馈、统计、指派等各处按 ID 反查名称。此前这些映射以硬编码 Map 形式散落在业务类中，
 * 新增城市必须改 Java 代码，且与数据库设计文档中的字典表完全脱节。</p>
 *
 * <p>刻意<b>不</b>在 {@code @PostConstruct} 中加载：种子数据由 {@code DataInitializer}
 * （CommandLineRunner）写入，而 {@code @PostConstruct} 早于所有 runner 执行，
 * 那时表还是空的，会导致整个字典缓存为空。改为首次访问时惰性加载，缓存后不再查询。</p>
 */
@Service
public class DictionaryService {
    private final GridProvinceRepository gridProvinceRepository;
    private final GridCityRepository gridCityRepository;
    private final AqiLevelRepository aqiLevelRepository;

    private volatile Map<Long, GridProvince> provinceById = Map.of();
    private volatile Map<Long, GridCity> cityById = Map.of();
    private volatile Map<Long, AqiLevel> levelById = Map.of();

    public DictionaryService(
            GridProvinceRepository gridProvinceRepository,
            GridCityRepository gridCityRepository,
            AqiLevelRepository aqiLevelRepository) {
        this.gridProvinceRepository = gridProvinceRepository;
        this.gridCityRepository = gridCityRepository;
        this.aqiLevelRepository = aqiLevelRepository;
    }

    /**
     * 重新加载字典缓存。运行期字典变动（新增城市、调整级别）后可调用，无需重启。
     */
    public synchronized void reload() {
        provinceById = gridProvinceRepository.findAll().stream()
                .collect(Collectors.toMap(GridProvince::getProvinceId, Function.identity()));

        cityById = gridCityRepository.findAll().stream()
                .collect(Collectors.toMap(GridCity::getCityId, Function.identity()));

        levelById = aqiLevelRepository.findAll().stream()
                .collect(Collectors.toMap(AqiLevel::getLevelId, Function.identity()));
    }

    private void ensureLoaded() {
        if (cityById.isEmpty()) {
            reload();
        }
    }

    public String provinceIdToName(Long provinceId) {
        if (provinceId == null) {
            return null;
        }

        ensureLoaded();
        GridProvince province = provinceById.get(provinceId);
        return province == null ? null : province.getProvinceName();
    }

    /**
     * 市级 ID 反查所属省级 ID。返回 null 表示该城市未登记在字典表中，
     * 调用方需自行决定如何处理（如跨省判定时拒绝），不要再退化成 0 这类伪 ID。
     */
    public Long cityIdToProvinceId(Long cityId) {
        if (cityId == null) {
            return null;
        }

        ensureLoaded();
        GridCity city = cityById.get(cityId);
        return city == null ? null : city.getProvinceId();
    }

    public String cityIdToCityName(Long cityId) {
        return cityName(cityId).orElse(null);
    }

    /**
     * 面向前端"市 + 所属省"的选项列表，可按省筛选（provinceId 为空表示全部）。
     */
    public List<CityOptionResponse> cityOptions(Long provinceId) {
        ensureLoaded();

        return cityById.values().stream()
                .filter(city -> Integer.valueOf(StatusFlags.ENABLED).equals(city.getStatus()))
                .filter(city -> provinceId == null || provinceId.equals(city.getProvinceId()))
                .sorted(Comparator
                        .comparing(GridCity::getSortNo, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(GridCity::getCityId))
                .map(city -> new CityOptionResponse(
                        city.getCityId(),
                        city.getProvinceId(),
                        city.getCityCode(),
                        city.getCityName(),
                        provinceIdToName(city.getProvinceId()),
                        city.getStatus()))
                .toList();
    }

    public String levelIdToLevelName(Long levelId) {
        if (levelId == null) {
            return null;
        }

        ensureLoaded();
        AqiLevel level = levelById.get(levelId);
        return level == null ? null : level.getLevelName();
    }

    /**
     * 全部级别名称，按 level_id 升序（即 AQI 由好到差的顺序）。
     */
    public List<String> aqiLevelNames() {
        ensureLoaded();

        return levelById.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().getLevelName())
                .toList();
    }

    /**
     * AQI 级别字典，供前端渲染级别下拉（含健康影响与防护建议）。
     */
    public List<AqiLevelResponse> aqiLevels() {
        ensureLoaded();

        return levelById.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    AqiLevel level = entry.getValue();
                    return new AqiLevelResponse(
                            level.getLevelId(),
                            level.getLevelName(),
                            level.getMinAqi(),
                            level.getMaxAqi(),
                            level.getHealthEffect(),
                            level.getSuggestion());
                })
                .toList();
    }

    /**
     * AQI 数值换算为级别。区间取自 aqi_level 表的 min_aqi / max_aqi，
     * 因此调整分级标准只需改数据，不用动计算逻辑。
     */
    public Optional<AqiLevel> levelByAqiValue(int aqiValue) {
        ensureLoaded();

        return levelById.values().stream()
                .filter(level -> level.getMinAqi() != null && level.getMaxAqi() != null)
                .filter(level -> aqiValue >= level.getMinAqi() && aqiValue <= level.getMaxAqi())
                .min(Comparator.comparing(AqiLevel::getMinAqi));
    }

    public String levelNameByAqiValue(int aqiValue) {
        return levelByAqiValue(aqiValue).map(AqiLevel::getLevelName).orElse(null);
    }

    public Optional<AqiLevel> levelByName(String levelName) {
        if (levelName == null) {
            return Optional.empty();
        }

        ensureLoaded();

        return levelById.values().stream()
                .filter(level -> levelName.equals(level.getLevelName()))
                .findFirst();
    }

    /**
     * 级别名称反查级别 ID。
     */
    public Long levelIdByName(String levelName) {
        return levelByName(levelName).map(AqiLevel::getLevelId).orElse(null);
    }

    private Optional<String> cityName(Long cityId) {
        if (cityId == null) {
            return Optional.empty();
        }

        ensureLoaded();
        GridCity city = cityById.get(cityId);
        return city == null ? Optional.empty() : Optional.of(city.getCityName());
    }
}
