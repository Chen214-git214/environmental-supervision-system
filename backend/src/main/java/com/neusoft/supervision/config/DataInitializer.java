package com.neusoft.supervision.config;

import com.neusoft.supervision.auth.entity.SysAdmin;
import com.neusoft.supervision.auth.repository.SysAdminRepository;
import com.neusoft.supervision.feedback.entity.AirQualityStat;
import com.neusoft.supervision.feedback.entity.AqiLevel;
import com.neusoft.supervision.feedback.entity.GridCity;
import com.neusoft.supervision.feedback.entity.GridMember;
import com.neusoft.supervision.feedback.entity.GridProvince;
import com.neusoft.supervision.feedback.entity.PublicFeedback;
import com.neusoft.supervision.feedback.entity.PublicSupervisor;
import com.neusoft.supervision.feedback.repository.AirQualityStatRepository;
import com.neusoft.supervision.feedback.repository.AqiLevelRepository;
import com.neusoft.supervision.feedback.repository.GridCityRepository;
import com.neusoft.supervision.feedback.repository.GridMemberRepository;
import com.neusoft.supervision.feedback.repository.GridProvinceRepository;
import com.neusoft.supervision.feedback.repository.PublicFeedbackRepository;
import com.neusoft.supervision.feedback.repository.PublicSupervisorRepository;
import com.neusoft.supervision.feedback.service.DictionaryService;
import com.neusoft.supervision.feedback.support.DictionarySeed;
import com.neusoft.supervision.feedback.support.StatusFlags;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 开发环境种子数据。字典表先于业务数据写入，后续记录才能按 ID 反查到名称。
 *
 * <p><b>注意</b>：以下姓名、手机号、身份证号均为演示用的虚构数据，不对应任何真实个人；
 * 演示账号密码（如 123456）仅供本地体验，生产环境必须替换。</p>
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private static final int STATUS_ENABLED = StatusFlags.ENABLED;

    private final PublicFeedbackRepository publicFeedbackRepository;
    private final GridMemberRepository gridMemberRepository;
    private final PublicSupervisorRepository publicSupervisorRepository;
    private final GridProvinceRepository gridProvinceRepository;
    private final GridCityRepository gridCityRepository;
    private final AqiLevelRepository aqiLevelRepository;
    private final SysAdminRepository sysAdminRepository;
    private final AirQualityStatRepository airQualityStatRepository;
    private final DictionaryService dictionaryService;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(
            PublicFeedbackRepository publicFeedbackRepository,
            GridMemberRepository gridMemberRepository,
            PublicSupervisorRepository publicSupervisorRepository,
            GridProvinceRepository gridProvinceRepository,
            GridCityRepository gridCityRepository,
            AqiLevelRepository aqiLevelRepository,
            SysAdminRepository sysAdminRepository,
            AirQualityStatRepository airQualityStatRepository,
            DictionaryService dictionaryService,
            BCryptPasswordEncoder passwordEncoder) {
        this.publicFeedbackRepository = publicFeedbackRepository;
        this.gridMemberRepository = gridMemberRepository;
        this.publicSupervisorRepository = publicSupervisorRepository;
        this.gridProvinceRepository = gridProvinceRepository;
        this.gridCityRepository = gridCityRepository;
        this.aqiLevelRepository = aqiLevelRepository;
        this.sysAdminRepository = sysAdminRepository;
        this.airQualityStatRepository = airQualityStatRepository;
        this.dictionaryService = dictionaryService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initDictionaries();

        // 系统管理员账号，密码 BCrypt 存储。显式指定 admin_id = 1，与种子反馈的 admin_id 对应。
        if (sysAdminRepository.count() == 0) {
            SysAdmin admin = new SysAdmin();
            admin.setAdminId(1L);
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123"));
            admin.setRealName("系统管理员");
            admin.setPhone("13800000000");
            admin.setRoleName("系统管理员");
            admin.setStatus(STATUS_ENABLED);
            admin.setCreateTime(LocalDateTime.now());
            sysAdminRepository.save(admin);
        }

        if (gridMemberRepository.count() == 0) {
            gridMemberRepository.save(createGridMember("SY-001", "刘强", "13900001001", 1L, "辽宁省", 1L, "沈阳市", "和平区一网格", 1));
            gridMemberRepository.save(createGridMember("SY-002", "赵敏", "13900001002", 1L, "辽宁省", 1L, "沈阳市", "沈河区二网格", 1));
            gridMemberRepository.save(createGridMember("DL-001", "陈晨", "13900002001", 1L, "辽宁省", 2L, "大连市", "中山区一网格", 1));
            gridMemberRepository.save(createGridMember("AS-001", "停用人员", "13900003001", 1L, "辽宁省", 3L, "鞍山市", "铁东区一网格", 0));
            gridMemberRepository.save(createGridMember("BJ-001", "周远", "13900004001", 2L, "北京市", 4L, "北京市", "朝阳区一网格", 1));
            gridMemberRepository.save(createGridMember("TJ-001", "孙宁", "13900005001", 3L, "天津市", 5L, "天津市", "和平区一网格", 1));
        }

        // 演示用公众监督员账号，密码统一为 123456（存储 BCrypt 摘要）。与反馈种子数据的 supervisor_id 1~3 对应。
        // 按 ID 倒序插入：H2 的自增列只在插入值达到当前序列时才推进序列，倒序插入可让 ID 精确落在 1~3，
        // 后续注册的新账号从 4 开始；若正序插入会因序列被推高而整体错位（1 变 2、2 变 3）。
        if (publicSupervisorRepository.count() == 0) {
            publicSupervisorRepository.save(createSupervisor(
                    3L, "supervisor003", "123456", "王五", "13800000003", "210102199901010033"));
            publicSupervisorRepository.save(createSupervisor(
                    2L, "supervisor002", "123456", "李四", "13800000002", "210102199901010022"));
            publicSupervisorRepository.save(createSupervisor(
                    1L, "supervisor001", "123456", "张三", "13800000001", "210102199901010011"));
        }

        if (publicFeedbackRepository.count() > 0) {
            return;
        }

        publicFeedbackRepository.save(createFeedback(
                1L,
                1L,
                3L,
                "扬尘",
                "沈阳市和平区南京北街100号",
                "施工现场未覆盖防尘网，扬尘明显。",
                "https://example.com/images/feedback-1.jpg",
                LocalDateTime.now().minusDays(2)));

        publicFeedbackRepository.save(createFeedback(
                2L,
                2L,
                2L,
                "噪声",
                "大连市中山区人民路88号",
                "夜间施工噪声较大，影响周边居民休息。",
                "https://example.com/images/feedback-2.jpg",
                LocalDateTime.now().minusDays(1)));

        publicFeedbackRepository.save(createFeedback(
                3L,
                3L,
                4L,
                "水污染",
                "鞍山市铁东区胜利南路66号",
                "排水口附近有异味，水体颜色异常。",
                "https://example.com/images/feedback-3.jpg",
                LocalDateTime.now().minusHours(6)));

        publicFeedbackRepository.save(createMeasuredFeedback(
                1L,
                1L,
                3L,
                "空气污染",
                "沈阳市浑南区创新路12号",
                "园区周边空气异味明显，PM2.5 浓度偏高。",
                "https://example.com/images/feedback-4.jpg",
                new BigDecimal("180"),
                new BigDecimal("3.2"),
                new BigDecimal("96"),
                "轻度污染",
                "良",
                "轻度污染",
                "轻度污染",
                LocalDateTime.now().minusMonths(1).minusDays(4)));

        publicFeedbackRepository.save(createMeasuredFeedback(
                2L,
                2L,
                3L,
                "工业废气",
                "大连市甘井子区海北路46号",
                "企业排气口附近气味刺鼻，SO2 监测值超标。",
                "https://example.com/images/feedback-5.jpg",
                new BigDecimal("220"),
                new BigDecimal("5.6"),
                new BigDecimal("68"),
                "轻度污染",
                "轻度污染",
                "良",
                "轻度污染",
                LocalDateTime.now().minusMonths(1).minusDays(1)));

        publicFeedbackRepository.save(createMeasuredFeedback(
                3L,
                4L,
                4L,
                "交通污染",
                "北京市朝阳区东三环北路8号",
                "主干道早晚高峰尾气积聚，CO 与颗粒物偏高。",
                "https://example.com/images/feedback-6.jpg",
                new BigDecimal("90"),
                new BigDecimal("6.2"),
                new BigDecimal("126"),
                "良",
                "轻度污染",
                "中度污染",
                "中度污染",
                LocalDateTime.now().minusDays(12)));

        publicFeedbackRepository.save(createMeasuredFeedback(
                1L,
                5L,
                2L,
                "锅炉废气",
                "天津市和平区营口道20号",
                "供热站周边短时烟气明显，SO2 浓度较高。",
                "https://example.com/images/feedback-7.jpg",
                new BigDecimal("310"),
                new BigDecimal("2.8"),
                new BigDecimal("82"),
                "中度污染",
                "良",
                "轻度污染",
                "中度污染",
                LocalDateTime.now().minusDays(5)));

        seedAirQualityStats();
    }

    /**
     * 为已实测的反馈补写 air_quality_stat 记录，使监测统计接口在冷启动后即有数据。
     * 这些种子反馈没有指派网格员，因此按所属城市取第一位在岗网格员作为上报人。
     */
    private void seedAirQualityStats() {
        if (airQualityStatRepository.count() > 0) {
            return;
        }

        List<PublicFeedback> measured = publicFeedbackRepository.findAll().stream()
                .filter(feedback -> feedback.getMeasuredAt() != null)
                .toList();

        for (PublicFeedback feedback : measured) {
            Long gridMemberId = gridMemberRepository
                    .findByCityIdAndStatusOrderByRealNameAsc(feedback.getCityId(), STATUS_ENABLED)
                    .stream()
                    .map(GridMember::getGridMemberId)
                    .findFirst()
                    .orElse(null);

            if (gridMemberId == null) {
                continue;
            }

            Long levelId = dictionaryService.levelIdByName(feedback.getAqiLevel());

            // aqi_value 与级别都齐全才写监测记录：早期数据可能只有等级文字而没有 AQI 数值，
            // 这类记录宁可不补，也不要凭空造一个数值出来。
            if (levelId == null || feedback.getAqiValue() == null) {
                continue;
            }

            AirQualityStat stat = new AirQualityStat();
            stat.setFeedbackId(feedback.getFeedbackId());
            stat.setCityId(feedback.getCityId());
            stat.setGridMemberId(gridMemberId);
            stat.setLevelId(levelId);
            stat.setAqiValue(feedback.getAqiValue());
            stat.setSo2(feedback.getSo2Value());
            stat.setCo(feedback.getCoValue());
            stat.setPm25(feedback.getPm25Value());
            stat.setMonitorTime(feedback.getMeasuredAt());
            stat.setCreateTime(LocalDateTime.now());
            airQualityStatRepository.save(stat);
        }
    }

    private PublicFeedback createFeedback(
            Long supervisorId,
            Long cityId,
            Long levelId,
            String pollutionType,
            String address,
            String description,
            String imageUrl,
            LocalDateTime submittedAt) {
        PublicFeedback feedback = new PublicFeedback();
        feedback.setSupervisorId(supervisorId);
        feedback.setCityId(cityId);
        feedback.setLevelId(levelId);
        feedback.setPollutionType(pollutionType);
        feedback.setAddress(address);
        feedback.setLongitude(new BigDecimal("123.414662"));
        feedback.setLatitude(new BigDecimal("41.799921"));
        feedback.setDescription(description);
        feedback.setImageUrl(imageUrl);
        feedback.setStatus(0);
        feedback.setSubmittedAt(submittedAt);
        return feedback;
    }

    private PublicFeedback createMeasuredFeedback(
            Long supervisorId,
            Long cityId,
            Long levelId,
            String pollutionType,
            String address,
            String description,
            String imageUrl,
            BigDecimal so2Value,
            BigDecimal coValue,
            BigDecimal pm25Value,
            String so2Level,
            String coLevel,
            String pm25Level,
            String aqiLevel,
            LocalDateTime submittedAt) {
        PublicFeedback feedback = createFeedback(
                supervisorId,
                cityId,
                levelId,
                pollutionType,
                address,
                description,
                imageUrl,
                submittedAt);
        feedback.setStatus(2);
        feedback.setSo2Value(so2Value);
        feedback.setCoValue(coValue);
        feedback.setPm25Value(pm25Value);
        feedback.setSo2Level(so2Level);
        feedback.setCoLevel(coLevel);
        feedback.setPm25Level(pm25Level);
        feedback.setAqiLevel(aqiLevel);
        feedback.setMeasuredAt(submittedAt.plusHours(4));
        return feedback;
    }

    /**
     * 省 / 市 / AQI 级别三张字典表。ID 显式指定，与反馈、网格员数据中的 ID 对应。
     */
    private void initDictionaries() {
        if (gridProvinceRepository.count() == 0) {
            for (DictionarySeed.ProvinceSeed seed : DictionarySeed.provinces()) {
                GridProvince province = new GridProvince();
                province.setProvinceId(seed.provinceId());
                province.setProvinceCode(seed.provinceCode());
                province.setProvinceName(seed.provinceName());
                province.setSortNo(seed.sortNo());
                province.setStatus(STATUS_ENABLED);
                province.setCreateTime(LocalDateTime.now());
                gridProvinceRepository.save(province);
            }
        }

        if (gridCityRepository.count() == 0) {
            for (DictionarySeed.CitySeed seed : DictionarySeed.cities()) {
                GridCity city = new GridCity();
                city.setCityId(seed.cityId());
                city.setProvinceId(seed.provinceId());
                city.setCityCode(seed.cityCode());
                city.setCityName(seed.cityName());
                city.setSortNo(seed.sortNo());
                city.setStatus(STATUS_ENABLED);
                city.setCreateTime(LocalDateTime.now());
                gridCityRepository.save(city);
            }
        }

        if (aqiLevelRepository.count() == 0) {
            for (DictionarySeed.AqiLevelSeed seed : DictionarySeed.aqiLevels()) {
                AqiLevel level = new AqiLevel();
                level.setLevelId(seed.levelId());
                level.setLevelName(seed.levelName());
                level.setMinAqi(seed.minAqi());
                level.setMaxAqi(seed.maxAqi());
                level.setHealthEffect(seed.healthEffect());
                level.setSuggestion(seed.suggestion());
                level.setCreateTime(LocalDateTime.now());
                aqiLevelRepository.save(level);
            }
        }
    }

    private PublicSupervisor createSupervisor(
            Long supervisorId,
            String username,
            String rawPassword,
            String realName,
            String phone,
            String idCard) {
        PublicSupervisor supervisor = new PublicSupervisor();
        supervisor.setSupervisorId(supervisorId);
        supervisor.setUsername(username);
        supervisor.setPassword(passwordEncoder.encode(rawPassword));
        supervisor.setRealName(realName);
        supervisor.setPhone(phone);
        supervisor.setIdCard(idCard);
        supervisor.setStatus(STATUS_ENABLED);
        supervisor.setRegisterTime(LocalDateTime.now());
        return supervisor;
    }

    private GridMember createGridMember(
            String employeeNo,
            String realName,
            String phone,
            Long provinceId,
            String provinceName,
            Long cityId,
            String cityName,
            String gridName,
            Integer status) {
        GridMember gridMember = new GridMember();
        gridMember.setEmployeeNo(employeeNo);
        gridMember.setRealName(realName);
        gridMember.setPhone(phone);
        gridMember.setProvinceId(provinceId);
        gridMember.setProvinceName(provinceName);
        gridMember.setCityId(cityId);
        gridMember.setCityName(cityName);
        gridMember.setGridName(gridName);
        gridMember.setStatus(status);
        return gridMember;
    }
}
