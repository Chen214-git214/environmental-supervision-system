package com.neusoft.supervision.feedback.service;

import com.neusoft.supervision.auth.entity.SysAdmin;
import com.neusoft.supervision.auth.repository.SysAdminRepository;
import com.neusoft.supervision.feedback.dto.AdminFeedbackResponse;
import com.neusoft.supervision.feedback.dto.AssignmentCandidatesResponse;
import com.neusoft.supervision.feedback.dto.FeedbackAssignRequest;
import com.neusoft.supervision.feedback.dto.FeedbackHandleRequest;
import com.neusoft.supervision.feedback.dto.FeedbackHandleResponse;
import com.neusoft.supervision.feedback.dto.FeedbackPageResponse;
import com.neusoft.supervision.feedback.dto.GridMeasurementSubmitRequest;
import com.neusoft.supervision.feedback.dto.GridMemberResponse;
import com.neusoft.supervision.feedback.dto.GridTaskResponse;
import com.neusoft.supervision.feedback.dto.MyFeedbackResponse;
import com.neusoft.supervision.feedback.dto.PublicFeedbackCreateRequest;
import com.neusoft.supervision.feedback.dto.PublicFeedbackResponse;
import com.neusoft.supervision.feedback.entity.AirQualityStat;
import com.neusoft.supervision.feedback.entity.GridMember;
import com.neusoft.supervision.feedback.entity.PublicFeedback;
import com.neusoft.supervision.feedback.entity.PublicSupervisor;
import com.neusoft.supervision.feedback.repository.AirQualityStatRepository;
import com.neusoft.supervision.feedback.repository.GridMemberRepository;
import com.neusoft.supervision.feedback.repository.PublicFeedbackRepository;
import com.neusoft.supervision.feedback.repository.PublicSupervisorRepository;
import com.neusoft.supervision.feedback.support.AqiCalculator;
import com.neusoft.supervision.feedback.support.AqiEvaluation;
import com.neusoft.supervision.feedback.support.StatusFlags;
import com.neusoft.supervision.feedback.support.StatusText;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PublicFeedbackService {
    private static final int STATUS_PENDING = StatusText.PENDING;
    private static final int STATUS_PROCESSING = StatusText.PROCESSING;
    private static final int STATUS_DONE = StatusText.DONE;
    private static final int STATUS_REJECTED = StatusText.REJECTED;
    private static final int GRID_MEMBER_ON_DUTY = StatusFlags.ENABLED;
    private static final String ASSIGNMENT_LOCAL = "LOCAL";
    private static final String ASSIGNMENT_CROSS_PROVINCE = "CROSS_PROVINCE";
    /** 每页最多返回的条数，防止 page size 过大导致全表扫描 */
    private static final int MAX_PAGE_SIZE = 100;

    private final PublicFeedbackRepository publicFeedbackRepository;
    private final GridMemberRepository gridMemberRepository;
    private final PublicSupervisorRepository publicSupervisorRepository;
    private final SysAdminRepository sysAdminRepository;
    private final AirQualityStatRepository airQualityStatRepository;
    private final DictionaryService dictionaryService;

    public PublicFeedbackService(
            PublicFeedbackRepository publicFeedbackRepository,
            GridMemberRepository gridMemberRepository,
            PublicSupervisorRepository publicSupervisorRepository,
            SysAdminRepository sysAdminRepository,
            AirQualityStatRepository airQualityStatRepository,
            DictionaryService dictionaryService) {
        this.publicFeedbackRepository = publicFeedbackRepository;
        this.gridMemberRepository = gridMemberRepository;
        this.publicSupervisorRepository = publicSupervisorRepository;
        this.sysAdminRepository = sysAdminRepository;
        this.airQualityStatRepository = airQualityStatRepository;
        this.dictionaryService = dictionaryService;
    }

    @Transactional(readOnly = true)
    public FeedbackPageResponse<AdminFeedbackResponse> findPage(int page, int size, Long cityId, Integer status) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Page<PublicFeedback> feedbackPage = publicFeedbackRepository.search(
                cityId, status, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "submittedAt")));

        // 一次查出本页涉及的监督员，避免每行一次查询
        Map<Long, SupervisorView> supervisorViews = loadSupervisors(
                feedbackPage.getContent().stream()
                        .map(PublicFeedback::getSupervisorId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));

        return new FeedbackPageResponse<>(
                feedbackPage.getContent().stream()
                        .map(feedback -> toAdminResponse(feedback, supervisorViews))
                        .toList(),
                feedbackPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminFeedbackResponse findDetail(Long afId) {
        PublicFeedback feedback = getFeedback(afId);
        return toAdminResponse(feedback, loadSupervisors(Set.of(feedback.getSupervisorId())));
    }

    @Transactional(readOnly = true)
    public List<GridTaskResponse> findGridTasks(Long gridMemberId, Integer status) {
        List<PublicFeedback> feedbackList = status == null
                ? publicFeedbackRepository.findByAssignedGridMemberIdOrderByAssignedAtDescSubmittedAtDesc(gridMemberId)
                : publicFeedbackRepository.findByAssignedGridMemberIdAndStatusOrderByAssignedAtDescSubmittedAtDesc(gridMemberId, status);

        return feedbackList.stream().map(this::toGridTaskResponse).toList();
    }

    @Transactional(readOnly = true)
    public GridTaskResponse findGridTaskDetail(Long afId, Long gridMemberId) {
        PublicFeedback feedback = getFeedback(afId);
        ensureAssignedToGridMember(feedback, gridMemberId);
        return toGridTaskResponse(feedback);
    }

    @Transactional(readOnly = true)
    public AssignmentCandidatesResponse findLocalAssignmentCandidates(Long afId) {
        PublicFeedback feedback = getFeedback(afId);
        List<GridMember> localMembers = gridMemberRepository
                .findByCityIdAndStatusOrderByRealNameAsc(feedback.getCityId(), GRID_MEMBER_ON_DUTY);

        return buildCandidatesResponse(
                feedback,
                localMembers,
                !localMembers.isEmpty(),
                localMembers.isEmpty(),
                localMembers.isEmpty() ? "本市暂无在岗网格员，可申请外省调派。" : "已匹配本市在岗网格员，仅允许本地指派。");
    }

    @Transactional(readOnly = true)
    public AssignmentCandidatesResponse findCrossProvinceAssignmentCandidates(Long afId) {
        PublicFeedback feedback = getFeedback(afId);
        boolean hasLocalMembers = hasLocalGridMember(feedback);

        if (hasLocalMembers) {
            return buildCandidatesResponse(
                    feedback,
                    List.of(),
                    true,
                    false,
                    "本市存在在岗网格员，外省调派不可用。");
        }

        List<GridMember> crossProvinceMembers = gridMemberRepository
                .findByProvinceIdNotAndStatusOrderByProvinceNameAscCityNameAscRealNameAsc(
                        provinceIdOf(feedback.getCityId()),
                        GRID_MEMBER_ON_DUTY);

        return buildCandidatesResponse(
                feedback,
                crossProvinceMembers,
                false,
                true,
                "本市暂无在岗网格员，可选择外省网格员兜底指派。");
    }

    @Transactional
    public AdminFeedbackResponse assignGridMember(Long afId, FeedbackAssignRequest request) {
        PublicFeedback feedback = getFeedback(afId);

        if (feedback.getAssignedGridMemberId() != null) {
            throw new IllegalArgumentException("当前反馈已指派网格员，请勿重复指派");
        }

        GridMember gridMember = gridMemberRepository.findById(request.getGridMemberId())
                .orElseThrow(() -> new IllegalArgumentException("网格员不存在"));

        if (!Integer.valueOf(GRID_MEMBER_ON_DUTY).equals(gridMember.getStatus())) {
            throw new IllegalArgumentException("网格员不在岗，无法指派");
        }

        boolean crossProvince = Boolean.TRUE.equals(request.getCrossProvince());
        boolean hasLocalMembers = hasLocalGridMember(feedback);

        if (crossProvince) {
            if (hasLocalMembers) {
                throw new IllegalArgumentException("本市存在在岗网格员，不能发起外省调派");
            }

            Long feedbackProvinceId = provinceIdOf(feedback.getCityId());

            if (feedbackProvinceId == null) {
                throw new IllegalArgumentException("反馈所属城市未登记在网格字典中，无法判定是否跨省");
            }

            if (feedbackProvinceId.equals(gridMember.getProvinceId())) {
                throw new IllegalArgumentException("外省调派必须选择外省在岗网格员");
            }
        } else if (!feedback.getCityId().equals(gridMember.getCityId())) {
            throw new IllegalArgumentException("本地指派仅允许选择反馈所属城市的网格员");
        }

        feedback.setAssignedGridMemberId(gridMember.getGridMemberId());
        feedback.setAssignedGridMemberName(gridMember.getRealName());
        feedback.setAssignmentType(crossProvince ? ASSIGNMENT_CROSS_PROVINCE : ASSIGNMENT_LOCAL);
        feedback.setAssignedAt(LocalDateTime.now());
        feedback.setStatus(STATUS_PROCESSING);

        return toAdminResponse(publicFeedbackRepository.save(feedback), null);
    }

    @Transactional
    public GridTaskResponse submitMeasurement(Long afId, Long gridMemberId, GridMeasurementSubmitRequest request) {
        PublicFeedback feedback = getFeedback(afId);

        // 先判状态再判归属：已完成/已驳回的任务重复提交应提示"不能重复提交"，而不是误导为"未指派"
        if (!Integer.valueOf(STATUS_PROCESSING).equals(feedback.getStatus()) || feedback.getMeasuredAt() != null) {
            throw new IllegalArgumentException("当前任务不是待检测状态，不能重复提交实测数据");
        }

        ensureAssignedToGridMember(feedback, gridMemberId);

        // 等级与 AQI 一律服务端计算，客户端只提供浓度值
        AqiEvaluation evaluation = AqiCalculator.evaluate(
                request.getSo2Value(), request.getCoValue(), request.getPm25Value());
        Long aqiLevelId = evaluation.aqiLevelId();

        feedback.setSo2Value(request.getSo2Value());
        feedback.setCoValue(request.getCoValue());
        feedback.setPm25Value(request.getPm25Value());
        feedback.setSo2Level(dictionaryService.levelIdToLevelName(evaluation.so2LevelId()));
        feedback.setCoLevel(dictionaryService.levelIdToLevelName(evaluation.coLevelId()));
        feedback.setPm25Level(dictionaryService.levelIdToLevelName(evaluation.pm25LevelId()));
        feedback.setAqiValue(evaluation.aqiValue());
        feedback.setAqiLevel(dictionaryService.levelIdToLevelName(aqiLevelId));
        feedback.setMeasuredAt(LocalDateTime.now());
        // 实测完成不代表办结，仍需管理员处理（回复并办结或驳回），因此这里停在"处理中"
        feedback.setStatus(STATUS_PROCESSING);

        PublicFeedback saved = publicFeedbackRepository.save(feedback);

        // 同时写入 air_quality_stat：实测数据的权威记录，同一反馈可留多次测量
        saveAirQualityStat(saved, gridMemberId, request, evaluation, aqiLevelId);

        return toGridTaskResponse(saved);
    }

    private void saveAirQualityStat(
            PublicFeedback feedback,
            Long gridMemberId,
            GridMeasurementSubmitRequest request,
            AqiEvaluation evaluation,
            Long aqiLevelId) {
        AirQualityStat stat = new AirQualityStat();
        stat.setFeedbackId(feedback.getFeedbackId());
        stat.setCityId(feedback.getCityId());
        stat.setGridMemberId(gridMemberId);
        stat.setLevelId(aqiLevelId);
        stat.setAqiValue(evaluation.aqiValue());
        stat.setSo2(request.getSo2Value());
        stat.setCo(request.getCoValue());
        stat.setPm25(request.getPm25Value());
        stat.setPm10(request.getPm10Value());
        stat.setNo2(request.getNo2Value());
        stat.setO3(request.getO3Value());
        stat.setMonitorTime(feedback.getMeasuredAt());
        stat.setCreateTime(LocalDateTime.now());

        airQualityStatRepository.save(stat);
    }

    /**
     * 管理员处理反馈：填写回复并将状态置为已处理（2）或已驳回（3）。
     *
     * <p>状态机：待处理(0) / 处理中(1) → 已处理(2) 或 已驳回(3)；已是终态则拒绝，避免重复处理与状态回退。</p>
     */
    @Transactional
    public FeedbackHandleResponse handleFeedback(Long afId, Long adminId, FeedbackHandleRequest request) {
        PublicFeedback feedback = getFeedback(afId);
        Integer requested = request.getStatus();

        if (requested == null || (requested != STATUS_DONE && requested != STATUS_REJECTED)) {
            throw new IllegalArgumentException("处理后状态只能是已处理(2)或已驳回(3)");
        }

        Integer current = feedback.getStatus();

        if (current != null && (current == STATUS_DONE || current == STATUS_REJECTED)) {
            throw new IllegalArgumentException(
                    "当前反馈已是终态（" + StatusText.of(current) + "），不能重复处理");
        }

        String reply = request.getReply().trim();

        if (reply.isEmpty()) {
            throw new IllegalArgumentException("处理回复不能为空");
        }

        feedback.setAdminId(adminId);
        feedback.setReplyContent(reply);
        feedback.setHandledAt(LocalDateTime.now());
        feedback.setStatus(requested);

        return toHandleResponse(publicFeedbackRepository.save(feedback));
    }

    private FeedbackHandleResponse toHandleResponse(PublicFeedback feedback) {
        FeedbackHandleResponse response = new FeedbackHandleResponse();
        response.setFeedbackId(feedback.getFeedbackId());
        response.setStatus(feedback.getStatus());
        response.setStatusText(StatusText.of(feedback.getStatus()));
        response.setReply(feedback.getReplyContent());
        response.setHandlerAdminId(feedback.getAdminId());
        response.setHandlerName(adminName(feedback.getAdminId()));
        response.setHandledAt(feedback.getHandledAt());
        response.setAssignedAt(feedback.getAssignedAt());
        response.setMeasuredAt(feedback.getMeasuredAt());
        return response;
    }

    /**
     * 处理人展示名。管理员已落入 sys_admin 表，这里按编号反查真实姓名。
     */
    private String adminName(Long adminId) {
        if (adminId == null) {
            return "-";
        }

        return sysAdminRepository.findById(adminId)
                .map(SysAdmin::getRealName)
                .filter(name -> name != null && !name.isBlank())
                .orElse("未知管理员(" + adminId + ")");
    }

    @Transactional
    public PublicFeedbackResponse create(Long supervisorId, PublicFeedbackCreateRequest request) {
        PublicFeedback feedback = new PublicFeedback();
        feedback.setSupervisorId(supervisorId);
        feedback.setCityId(request.getCityId());
        feedback.setLevelId(request.getLevelId());
        feedback.setPollutionType(request.getPollutionType());
        feedback.setAddress(request.getAddress());
        feedback.setLongitude(request.getLongitude());
        feedback.setLatitude(request.getLatitude());
        feedback.setDescription(request.getDescription());
        feedback.setImageUrl(request.getImageUrl());
        feedback.setStatus(STATUS_PENDING);
        feedback.setSubmittedAt(LocalDateTime.now());

        return toResponse(publicFeedbackRepository.save(feedback));
    }

    private PublicFeedback getFeedback(Long afId) {
        return publicFeedbackRepository.findById(afId)
                .orElseThrow(() -> new IllegalArgumentException("反馈不存在"));
    }

    private void ensureAssignedToGridMember(PublicFeedback feedback, Long gridMemberId) {
        if (gridMemberId == null || !gridMemberId.equals(feedback.getAssignedGridMemberId())) {
            throw new IllegalArgumentException("当前任务未指派给该网格员");
        }
    }

    private PublicFeedbackResponse toResponse(PublicFeedback feedback) {
        PublicFeedbackResponse response = new PublicFeedbackResponse();
        response.setFeedbackId(feedback.getFeedbackId());
        response.setSupervisorId(feedback.getSupervisorId());
        response.setCityId(feedback.getCityId());
        response.setCityName(cityNameOf(feedback.getCityId()));
        response.setPollutionType(feedback.getPollutionType());
        response.setAddress(feedback.getAddress());
        response.setStatus(feedback.getStatus());
        response.setStatusText(StatusText.of(feedback.getStatus()));
        response.setSubmittedAt(feedback.getSubmittedAt());
        return response;
    }

    /**
     * 公众监督员查看自己提交的反馈及处理进展。
     */
    @Transactional(readOnly = true)
    public List<MyFeedbackResponse> findMyFeedback(Long supervisorId) {
        return publicFeedbackRepository.findBySupervisorIdOrderBySubmittedAtDesc(supervisorId)
                .stream()
                .map(this::toMyFeedbackResponse)
                .toList();
    }

    private MyFeedbackResponse toMyFeedbackResponse(PublicFeedback feedback) {
        MyFeedbackResponse response = new MyFeedbackResponse();
        response.setFeedbackId(feedback.getFeedbackId());
        response.setCityId(feedback.getCityId());
        response.setCityName(cityNameOf(feedback.getCityId()));
        response.setPollutionType(feedback.getPollutionType());
        response.setAddress(feedback.getAddress());
        response.setDescription(feedback.getDescription());
        response.setStatus(feedback.getStatus());
        response.setStatusText(StatusText.of(feedback.getStatus()));
        response.setAssignedGridMemberName(feedback.getAssignedGridMemberName());
        response.setSubmittedAt(feedback.getSubmittedAt());
        response.setSo2Level(feedback.getSo2Level());
        response.setCoLevel(feedback.getCoLevel());
        response.setPm25Level(feedback.getPm25Level());
        response.setAqiLevel(feedback.getAqiLevel());
        response.setAqiValue(feedback.getAqiValue());
        response.setMeasuredAt(feedback.getMeasuredAt());
        response.setReplyContent(feedback.getReplyContent());
        response.setHandledAt(feedback.getHandledAt());
        return response;
    }

    private AdminFeedbackResponse toAdminResponse(PublicFeedback feedback, Map<Long, SupervisorView> supervisorViews) {
        AdminFeedbackResponse response = new AdminFeedbackResponse();
        response.setAfId(feedback.getFeedbackId());
        response.setFeedbackId(feedback.getFeedbackId());
        response.setSupervisorId(feedback.getSupervisorId());
        response.setSupervisorName(supervisorName(feedback.getSupervisorId(), supervisorViews));
        response.setSupervisorPhone(supervisorPhone(feedback.getSupervisorId(), supervisorViews));
        response.setCityId(feedback.getCityId());
        response.setCityName(cityNameOf(feedback.getCityId()));
        response.setLevelId(feedback.getLevelId());
        response.setEstimatedLevel(estimatedLevelOf(feedback.getLevelId()));
        response.setPollutionType(feedback.getPollutionType());
        response.setAddress(feedback.getAddress());
        response.setLongitude(feedback.getLongitude());
        response.setLatitude(feedback.getLatitude());
        response.setDescription(feedback.getDescription());
        response.setImageUrl(feedback.getImageUrl());
        response.setStatus(feedback.getStatus());
        response.setStatusText(StatusText.of(feedback.getStatus()));
        response.setAssignedGridMemberId(feedback.getAssignedGridMemberId());
        response.setAssignedGridMemberName(feedback.getAssignedGridMemberName());
        response.setAssignmentType(feedback.getAssignmentType());
        response.setAssignmentTypeText(assignmentTypeText(feedback.getAssignmentType()));
        response.setAssignedAt(feedback.getAssignedAt());
        response.setFeedbackDate(feedback.getSubmittedAt());
        response.setSo2Value(feedback.getSo2Value());
        response.setCoValue(feedback.getCoValue());
        response.setPm25Value(feedback.getPm25Value());
        response.setSo2Level(feedback.getSo2Level());
        response.setCoLevel(feedback.getCoLevel());
        response.setPm25Level(feedback.getPm25Level());
        response.setAqiLevel(feedback.getAqiLevel());
        response.setAqiValue(feedback.getAqiValue());
        response.setMeasuredAt(feedback.getMeasuredAt());
        response.setReplyContent(feedback.getReplyContent());
        response.setHandlerAdminId(feedback.getAdminId());
        response.setHandlerName(adminName(feedback.getAdminId()));
        response.setHandledAt(feedback.getHandledAt());
        response.setSubmittedAt(feedback.getSubmittedAt());
        return response;
    }

    private GridTaskResponse toGridTaskResponse(PublicFeedback feedback) {
        GridTaskResponse response = new GridTaskResponse();
        response.setAfId(feedback.getFeedbackId());
        response.setFeedbackId(feedback.getFeedbackId());
        response.setSupervisorId(feedback.getSupervisorId());
        response.setSupervisorName(supervisorName(feedback.getSupervisorId(), null));
        response.setSupervisorPhone(supervisorPhone(feedback.getSupervisorId(), null));
        response.setCityId(feedback.getCityId());
        response.setCityName(cityNameOf(feedback.getCityId()));
        response.setEstimatedLevel(estimatedLevelOf(feedback.getLevelId()));
        response.setPollutionType(feedback.getPollutionType());
        response.setAddress(feedback.getAddress());
        response.setLongitude(feedback.getLongitude());
        response.setLatitude(feedback.getLatitude());
        response.setDescription(feedback.getDescription());
        response.setImageUrl(feedback.getImageUrl());
        response.setStatus(feedback.getStatus());
        response.setStatusText(StatusText.of(feedback.getStatus()));
        response.setAssignedGridMemberId(feedback.getAssignedGridMemberId());
        response.setAssignedGridMemberName(feedback.getAssignedGridMemberName());
        response.setAssignedAt(feedback.getAssignedAt());
        response.setFeedbackDate(feedback.getSubmittedAt());
        response.setSo2Value(feedback.getSo2Value());
        response.setCoValue(feedback.getCoValue());
        response.setPm25Value(feedback.getPm25Value());
        response.setSo2Level(feedback.getSo2Level());
        response.setCoLevel(feedback.getCoLevel());
        response.setPm25Level(feedback.getPm25Level());
        response.setAqiLevel(feedback.getAqiLevel());
        response.setAqiValue(feedback.getAqiValue());
        response.setMeasuredAt(feedback.getMeasuredAt());
        return response;
    }

    private AssignmentCandidatesResponse buildCandidatesResponse(
            PublicFeedback feedback,
            List<GridMember> members,
            boolean hasLocalMembers,
            boolean crossProvinceEnabled,
            String message) {
        AssignmentCandidatesResponse response = new AssignmentCandidatesResponse();
        response.setAfId(feedback.getFeedbackId());
        response.setCityId(feedback.getCityId());
        response.setCityName(cityNameOf(feedback.getCityId()));
        response.setHasLocalMembers(hasLocalMembers);
        response.setCrossProvinceEnabled(crossProvinceEnabled);
        response.setMessage(message);
        response.setMembers(members.stream().map(this::toGridMemberResponse).toList());
        return response;
    }

    private GridMemberResponse toGridMemberResponse(GridMember gridMember) {
        GridMemberResponse response = new GridMemberResponse();
        response.setGridMemberId(gridMember.getGridMemberId());
        response.setEmployeeNo(gridMember.getEmployeeNo());
        response.setRealName(gridMember.getRealName());
        response.setPhone(gridMember.getPhone());
        response.setProvinceId(gridMember.getProvinceId());
        response.setProvinceName(gridMember.getProvinceName());
        response.setCityId(gridMember.getCityId());
        response.setCityName(gridMember.getCityName());
        response.setGridName(gridMember.getGridName());
        return response;
    }

    private boolean hasLocalGridMember(PublicFeedback feedback) {
        return gridMemberRepository.existsByCityIdAndStatus(feedback.getCityId(), GRID_MEMBER_ON_DUTY);
    }

    private String assignmentTypeText(String assignmentType) {
        if (ASSIGNMENT_LOCAL.equals(assignmentType)) {
            return "本市指派";
        }

        if (ASSIGNMENT_CROSS_PROVINCE.equals(assignmentType)) {
            return "外省调派";
        }

        return "未指派";
    }

    private Long provinceIdOf(Long cityId) {
        return dictionaryService.cityIdToProvinceId(cityId);
    }

    private String cityNameOf(Long cityId) {
        String cityName = dictionaryService.cityIdToCityName(cityId);

        if (cityName != null) {
            return cityName;
        }

        return cityId == null ? "-" : "未知城市(" + cityId + ")";
    }

    private String estimatedLevelOf(Long levelId) {
        if (levelId == null) {
            return "-";
        }

        String levelName = dictionaryService.levelIdToLevelName(levelId);
        return levelName == null ? "-" : levelName;
    }

    /**
     * 批量加载监督员信息。传入已查好的视图映射时直接复用，避免列表渲染按行查询。
     */
    private Map<Long, SupervisorView> loadSupervisors(Set<Long> supervisorIds) {
        if (supervisorIds == null || supervisorIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, SupervisorView> views = new LinkedHashMap<>();

        for (PublicSupervisor supervisor : publicSupervisorRepository.findAllById(supervisorIds)) {
            views.put(supervisor.getSupervisorId(), new SupervisorView(supervisor.getRealName(), supervisor.getPhone()));
        }

        return views;
    }

    /**
     * 反馈列表/详情单条渲染走这里；列表场景由调用方传入已批量查好的映射。
     */
    private SupervisorView supervisorView(Long supervisorId, Map<Long, SupervisorView> loaded) {
        if (supervisorId == null) {
            return null;
        }

        if (loaded != null) {
            return loaded.get(supervisorId);
        }

        return loadSupervisors(Set.of(supervisorId)).get(supervisorId);
    }

    private String supervisorName(Long supervisorId, Map<Long, SupervisorView> loaded) {
        if (supervisorId == null) {
            return "-";
        }

        SupervisorView view = supervisorView(supervisorId, loaded);

        if (view != null && view.realName() != null && !view.realName().isBlank()) {
            return view.realName();
        }

        // 账号已被删除或数据不完整时的兜底展示
        return "未知监督员(" + supervisorId + ")";
    }

    private String supervisorPhone(Long supervisorId, Map<Long, SupervisorView> loaded) {
        if (supervisorId == null) {
            return "-";
        }

        SupervisorView view = supervisorView(supervisorId, loaded);
        return view == null || view.phone() == null ? "-" : view.phone();
    }

    private record SupervisorView(String realName, String phone) {
    }
}
