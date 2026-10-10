package com.neusoft.supervision.feedback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.supervision.auth.entity.SysAdmin;
import com.neusoft.supervision.auth.repository.SysAdminRepository;
import com.neusoft.supervision.feedback.support.StatusFlags;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicFeedbackControllerTest {

    /** 注册用例需要唯一账号，避免用例之间互相干扰 */
    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysAdminRepository sysAdminRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String loginAsAdmin() throws Exception {
        return extractToken(mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminCode": "admin",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString());
    }

    private String loginAsGridMember(String employeeNo) throws Exception {
        return extractToken(mockMvc.perform(post("/api/auth/grid-member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNo": "%s",
                                  "password": "123"
                                }
                                """.formatted(employeeNo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString());
    }

    private String loginAsSupervisor(String username, String password) throws Exception {
        return extractToken(mockMvc.perform(post("/api/auth/public-supervisor/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString());
    }

    private String extractToken(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return root.path("data").path("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String registerBody(String username, String phone) {
        return """
                {
                  "username": "%s",
                  "password": "abc123456",
                  "realName": "测试监督员",
                  "phone": "%s",
                  "idCard": "210102199901010011"
                }
                """.formatted(username, phone);
    }

    /**
     * 生成 11 位且不与种子数据（1380000000x）冲突的手机号。
     */
    private String testPhone(String prefix, int seq) {
        return prefix + String.format("%08d", seq);
    }

    @Test
    void publicSupervisorCanRegisterAndLoginWithIssuedToken() throws Exception {
        int seq = SEQ.incrementAndGet();
        String username = "spr" + seq + "001";
        String phone = testPhone("137", seq);

        // 注册成功即签发令牌，可直接用于提交反馈
        String registerResponse = mockMvc.perform(post("/api/public-supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, phone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.supervisorInfo.supervisorId").isNumber())
                .andExpect(jsonPath("$.data.supervisorInfo.username", is(username)))
                .andExpect(jsonPath("$.data.supervisorInfo.status", is(1)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                // 响应绝不能带出密码字段
                .andExpect(jsonPath("$.data.supervisorInfo.password").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String registeredId = objectMapper.readTree(registerResponse)
                .path("data").path("supervisorInfo").path("supervisorId").asText();

        // 注册返回的令牌可用
        mockMvc.perform(get("/api/public-supervisors/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(extractToken(registerResponse))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supervisorId").value(Integer.parseInt(registeredId)));

        // 用注册时的密码能登录成功 -> 说明密码是按摘要校验的，不是明文比对
        String loginToken = loginAsSupervisor(username, "abc123456");
        mockMvc.perform(get("/api/public-supervisors/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is(username)));

        // 错误密码必须失败
        mockMvc.perform(post("/api/auth/public-supervisor/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "wrong-password"
                                }
                                """.formatted(username)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("监督员账号或密码错误")));
    }

    @Test
    void registerRejectsDuplicateUsernameAndPhone() throws Exception {
        int seq = SEQ.incrementAndGet();
        String username = "dup" + seq + "001";
        String phone = testPhone("139", seq);

        mockMvc.perform(post("/api/public-supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, phone)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public-supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, testPhone("139", seq + 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("该登录账号已被注册")));

        mockMvc.perform(post("/api/public-supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("other" + seq + "001", phone)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("该手机号已被注册")));
    }

    @Test
    void registerValidatesInput() throws Exception {
        // 密码过短
        mockMvc.perform(post("/api/public-supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "shortpwd001",
                                  "password": "123",
                                  "realName": "测试",
                                  "phone": "13700000001"
                                }
                                """))
                .andExpect(status().isBadRequest());

        // 手机号格式非法
        mockMvc.perform(post("/api/public-supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "badphone001",
                                  "password": "abc123456",
                                  "realName": "测试",
                                  "phone": "12345"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void supervisorSubmitsFeedbackWithTokenIdentityAndSeesOwnList() throws Exception {
        String token = bearer(loginAsSupervisor("supervisor001", "123456"));

        // 请求体不再传 supervisorId，身份完全来自令牌
        mockMvc.perform(post("/api/public-feedback")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cityId": 1,
                                  "levelId": 3,
                                  "pollutionType": "扬尘",
                                  "address": "沈阳市和平区南京北街100号",
                                  "longitude": 123.414662,
                                  "latitude": 41.799921,
                                  "description": "施工现场未覆盖防尘网，扬尘明显。",
                                  "imageUrl": "https://example.com/images/feedback-1.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.feedbackId").isNumber())
                .andExpect(jsonPath("$.data.supervisorId", is(1)))
                .andExpect(jsonPath("$.data.cityName", is("沈阳市")))
                .andExpect(jsonPath("$.data.status", is(0)))
                .andExpect(jsonPath("$.data.statusText", is("待处理")));

        // 即使请求体里塞了别人的 supervisorId 也会被忽略（DTO 已无该字段）
        mockMvc.perform(post("/api/public-feedback")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supervisorId": 999,
                                  "cityId": 1,
                                  "pollutionType": "异味",
                                  "address": "沈阳市和平区某处",
                                  "description": "验证 supervisorId 无法被伪造。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supervisorId", is(1)));

        mockMvc.perform(get("/api/public-feedback/mine").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data[0].feedbackId").isNumber())
                .andExpect(jsonPath("$.data[0].statusText", notNullValue()));
    }

    @Test
    void publicEndpointsRejectAnonymousAndWrongRole() throws Exception {
        // 未登录不能提交反馈，也不能查看自己的列表
        mockMvc.perform(post("/api/public-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cityId": 1,
                                  "pollutionType": "扬尘",
                                  "address": "沈阳市和平区",
                                  "description": "未登录提交，应被拒绝。"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(401)));

        mockMvc.perform(get("/api/public-feedback/mine"))
                .andExpect(status().isUnauthorized());

        // 注册接口无需登录
        mockMvc.perform(post("/api/public-supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("anon" + SEQ.incrementAndGet() + "01", "13600000001")))
                .andExpect(status().isOk());

        // 管理员令牌不能访问公众端接口
        String adminToken = bearer(loginAsAdmin());
        mockMvc.perform(get("/api/public-feedback/mine").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));

        mockMvc.perform(get("/api/public-supervisors/me").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isForbidden());

        // 公众监督员令牌不能访问管理端/网格端接口
        String supervisorToken = bearer(loginAsSupervisor("supervisor001", "123456"));
        mockMvc.perform(get("/api/feedback").header(HttpHeaders.AUTHORIZATION, supervisorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/grid/tasks").header(HttpHeaders.AUTHORIZATION, supervisorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/statistics/dashboard").header(HttpHeaders.AUTHORIZATION, supervisorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void dictionaryTablesResolveNamesInsteadOfHardcodedMaps() throws Exception {
        String adminToken = bearer(loginAsAdmin());

        // 监督员姓名来自 public_supervisor 表；城市名来自 grid_city；预计等级来自 aqi_level
        mockMvc.perform(get("/api/feedback/1").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supervisorName", is("张三")))
                .andExpect(jsonPath("$.data.supervisorPhone", is("13800000001")))
                .andExpect(jsonPath("$.data.cityName", is("沈阳市")))
                .andExpect(jsonPath("$.data.estimatedLevel", is("轻度污染")));

        // 城市字典：包含所属省名称与区划编码
        mockMvc.perform(get("/api/grid-cities").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(5)))
                .andExpect(jsonPath("$.data[0].cityName", is("沈阳市")))
                .andExpect(jsonPath("$.data[0].provinceName", is("辽宁省")))
                .andExpect(jsonPath("$.data[0].cityCode", is("210100")));

        // 按省筛选
        mockMvc.perform(get("/api/grid-cities")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("provinceId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(3)));

        // 公众监督员也需要城市字典（提交反馈时选城市）
        String supervisorToken = bearer(loginAsSupervisor("supervisor001", "123456"));
        mockMvc.perform(get("/api/grid-cities").header(HttpHeaders.AUTHORIZATION, supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(5)));

        // AQI 级别分布口径来自 aqi_level 字典表
        mockMvc.perform(get("/api/statistics/dashboard").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aqiDistribution.length()", is(6)))
                .andExpect(jsonPath("$.data.aqiDistribution[0].level", is("优")));

        // 未登录不能读取字典
        mockMvc.perform(get("/api/grid-cities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanHandlePendingFeedback() throws Exception {
        String adminToken = bearer(loginAsAdmin());

        // 种子 feedback 2（大连，未指派）仍为"待处理"，可直接办结
        mockMvc.perform(put("/api/feedback/2/handle")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 2,
                                  "reply": "经核查该处为临时施工，已要求施工方覆盖防尘网并洒水降尘。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedbackId", is(2)))
                .andExpect(jsonPath("$.data.status", is(2)))
                // 状态 2 的文案：数据库设计文档写作"已处理"，泳道图用时序图为"已完成"，实现统一取"已完成"
                .andExpect(jsonPath("$.data.statusText", is("已完成")))
                .andExpect(jsonPath("$.data.reply", is("经核查该处为临时施工，已要求施工方覆盖防尘网并洒水降尘。")))
                .andExpect(jsonPath("$.data.handlerAdminId", is(1)))
                .andExpect(jsonPath("$.data.handlerName", notNullValue()))
                .andExpect(jsonPath("$.data.handledAt", notNullValue()));

        // 处理结果落库：详情里能读到回复与处理人
        mockMvc.perform(get("/api/feedback/2").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.replyContent", is("经核查该处为临时施工，已要求施工方覆盖防尘网并洒水降尘。")))
                .andExpect(jsonPath("$.data.handlerName", is("系统管理员")))
                .andExpect(jsonPath("$.data.handledAt", notNullValue()));
    }

    @Test
    void handleRejectsIllegalTransitions() throws Exception {
        String adminToken = bearer(loginAsAdmin());
        String supervisorToken = bearer(loginAsSupervisor("supervisor002", "123456"));

        // 自建一条反馈，避免依赖其他用例的执行顺序
        String created = mockMvc.perform(post("/api/public-feedback")
                        .header(HttpHeaders.AUTHORIZATION, supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cityId": 2,
                                  "pollutionType": "噪声",
                                  "address": "大连市中山区某路段",
                                  "description": "用于验证状态机非法转移。"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long feedbackId = objectMapper.readTree(created).path("data").path("feedbackId").asLong();
        String path = "/api/feedback/" + feedbackId + "/handle";

        // 状态只能是 2 或 3
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 1,
                                  "reply": "试图改回处理中"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("处理后状态只能是已处理(2)或已驳回(3)")));

        // 回复不能为空（空白字符串也不行）
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 2,
                                  "reply": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());

        // 正常办结
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 2,
                                  "reply": "已办结。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is(2)));

        // 终态后不能再次处理（也不能改成驳回或回退）
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 3,
                                  "reply": "重复处理"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("当前反馈已是终态（已完成），不能重复处理")));

        // 不存在的反馈
        mockMvc.perform(put("/api/feedback/999999/handle")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 2,
                                  "reply": "反馈不存在"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("反馈不存在")));
    }

    @Test
    void handleRequiresAdminRole() throws Exception {
        // 未登录
        mockMvc.perform(put("/api/feedback/3/handle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 2,
                                  "reply": "未登录处理"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        // 网格员与公众监督员都无权处理
        mockMvc.perform(put("/api/feedback/3/handle")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAsGridMember("SY-001")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 2,
                                  "reply": "网格员处理"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/feedback/3/handle")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAsSupervisor("supervisor001", "123456")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 2,
                                  "reply": "监督员处理"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullLifecycleSubmitAssignMeasureHandleAndReject() throws Exception {
        String adminToken = bearer(loginAsAdmin());
        String gridMemberToken = bearer(loginAsGridMember("SY-002"));
        String supervisorToken = bearer(loginAsSupervisor("supervisor001", "123456"));

        // 1) 公众提交反馈 → 待处理
        String created = mockMvc.perform(post("/api/public-feedback")
                        .header(HttpHeaders.AUTHORIZATION, supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cityId": 1,
                                  "levelId": 2,
                                  "pollutionType": "工业废气",
                                  "address": "沈阳市沈河区某厂区",
                                  "description": "厂区排放异味明显，需现场核查。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is(0)))
                .andExpect(jsonPath("$.data.statusText", is("待处理")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long feedbackId = objectMapper.readTree(created).path("data").path("feedbackId").asLong();

        // 2) 管理员指派给本市网格员 → 处理中
        mockMvc.perform(put("/api/feedback/" + feedbackId + "/assign")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gridMemberId": 2,
                                  "crossProvince": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is(1)));

        // 3) 网格员实测 → 仍是处理中，但已算出 AQI
        mockMvc.perform(post("/api/grid/tasks/" + feedbackId + "/measurements")
                        .header(HttpHeaders.AUTHORIZATION, gridMemberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "so2Value": 40,
                                  "coValue": 1.2,
                                  "pm25Value": 120
                                }
                                """))
                .andExpect(status().isOk())
                // SO2=40→40、CO=1.2→30、PM2.5=120→157，AQI 取最大 157（中度污染）
                .andExpect(jsonPath("$.data.aqiValue", is(157)))
                .andExpect(jsonPath("$.data.aqiLevel", is("中度污染")))
                .andExpect(jsonPath("$.data.status", is(1)));

        // 4) 管理员驳回 → 终态，并写明理由
        mockMvc.perform(put("/api/feedback/" + feedbackId + "/handle")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 3,
                                  "reply": "现场核查未发现所述排放行为，本次反馈予以驳回。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is(3)))
                .andExpect(jsonPath("$.data.statusText", is("已驳回")))
                .andExpect(jsonPath("$.data.reply", is("现场核查未发现所述排放行为，本次反馈予以驳回。")));

        // 5) 公众监督员在自己列表里能看到回复与处理时间
        mockMvc.perform(get("/api/public-feedback/mine").header(HttpHeaders.AUTHORIZATION, supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].feedbackId", is((int) feedbackId)))
                .andExpect(jsonPath("$.data[0].statusText", is("已驳回")))
                .andExpect(jsonPath("$.data[0].replyContent", is("现场核查未发现所述排放行为，本次反馈予以驳回。")))
                .andExpect(jsonPath("$.data[0].handledAt", notNullValue()))
                .andExpect(jsonPath("$.data[0].aqiValue", is(157)));

        // 6) 已驳回的任务不能再次提交实测数据
        mockMvc.perform(post("/api/grid/tasks/" + feedbackId + "/measurements")
                        .header(HttpHeaders.AUTHORIZATION, gridMemberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "so2Value": 10,
                                  "coValue": 1,
                                  "pm25Value": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("当前任务不是待检测状态，不能重复提交实测数据")));
    }

    @Test
    void adminAccountComesFromSysAdminTable() throws Exception {
        // 登录响应带出 sys_admin 表里的真实姓名
        String loginBody = mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminCode": "admin",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adminInfo.id", is(1)))
                .andExpect(jsonPath("$.data.adminInfo.adminCode", is("admin")))
                .andExpect(jsonPath("$.data.adminInfo.name", is("系统管理员")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = bearer(extractToken(loginBody));

        // 处理反馈后，处理人显示为 sys_admin 中的真实姓名，而不是写死的文案
        String created = mockMvc.perform(post("/api/public-feedback")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAsSupervisor("supervisor001", "123456")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cityId": 1,
                                  "pollutionType": "扬尘",
                                  "address": "沈阳市铁西区某处",
                                  "description": "用于验证处理人姓名取自 sys_admin 表。"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long feedbackId = objectMapper.readTree(created).path("data").path("feedbackId").asLong();

        mockMvc.perform(put("/api/feedback/" + feedbackId + "/handle")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 2,
                                  "reply": "已办结，处理人姓名应来自数据库。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.handlerAdminId", is(1)))
                .andExpect(jsonPath("$.data.handlerName", is("系统管理员")));

        mockMvc.perform(get("/api/feedback/" + feedbackId).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.handlerName", is("系统管理员")));

        // 密码错误仍被拒绝
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminCode": "admin",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("管理员账号或密码错误")));

        // 不存在的账号
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminCode": "no-such-admin",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("管理员账号或密码错误")));
    }

    @Test
    void disabledAdminAccountCannotLogin() throws Exception {
        // 单独建一个停用账号，避免改动种子管理员而影响其他用例
        SysAdmin disabled = new SysAdmin();
        disabled.setUsername("disabled-admin");
        disabled.setPassword(passwordEncoder.encode("123"));
        disabled.setRealName("停用管理员");
        disabled.setRoleName("系统管理员");
        disabled.setStatus(StatusFlags.DISABLED);
        disabled.setCreateTime(LocalDateTime.now());
        sysAdminRepository.save(disabled);

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminCode": "disabled-admin",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("该管理员账号已被停用")));
    }

    @Test
    void measurementIsWrittenIntoAirQualityStatAndQueryable() throws Exception {
        String adminToken = bearer(loginAsAdmin());
        String gridToken = bearer(loginAsGridMember("SY-001"));
        String supervisorToken = bearer(loginAsSupervisor("supervisor001", "123456"));

        // 建一条反馈并指派给沈阳网格员
        String created = mockMvc.perform(post("/api/public-feedback")
                        .header(HttpHeaders.AUTHORIZATION, supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cityId": 1,
                                  "pollutionType": "扬尘",
                                  "address": "沈阳市于洪区某工地",
                                  "description": "用于验证实测数据写入 air_quality_stat。"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long feedbackId = objectMapper.readTree(created).path("data").path("feedbackId").asLong();

        mockMvc.perform(put("/api/feedback/" + feedbackId + "/assign")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gridMemberId": 1,
                                  "crossProvince": false
                                }
                                """))
                .andExpect(status().isOk());

        // 提交六项污染物
        mockMvc.perform(post("/api/grid/tasks/" + feedbackId + "/measurements")
                        .header(HttpHeaders.AUTHORIZATION, gridToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "so2Value": 60,
                                  "coValue": 3.5,
                                  "pm25Value": 82,
                                  "pm10Value": 120,
                                  "no2Value": 45,
                                  "o3Value": 88
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aqiValue", is(109)));

        // 统计接口按反馈筛选，能查到刚写入的记录，且六项浓度与级别齐全
        mockMvc.perform(get("/api/air-quality-stats")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("cityId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data[0].statId").isNumber())
                .andExpect(jsonPath("$.data[0].feedbackId").isNumber())
                .andExpect(jsonPath("$.data[0].cityName", is("沈阳市")))
                .andExpect(jsonPath("$.data[0].gridMemberName", is("刘强")))
                .andExpect(jsonPath("$.data[0].levelName", notNullValue()))
                .andExpect(jsonPath("$.data[0].aqi").isNumber())
                .andExpect(jsonPath("$.data[0].so2").isNumber())
                .andExpect(jsonPath("$.data[0].co").isNumber())
                .andExpect(jsonPath("$.data[0].pm25").isNumber())
                .andExpect(jsonPath("$.data[0].monitoredAt", notNullValue()));

        // 六项污染物确实都落库（取该反馈的记录逐项核对）
        String statsBody = mockMvc.perform(get("/api/air-quality-stats")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("cityId", "1"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode rows = objectMapper.readTree(statsBody).path("data");
        JsonNode mine = null;
        for (JsonNode row : rows) {
            if (row.path("feedbackId").asLong() == feedbackId) {
                mine = row;
                break;
            }
        }

        assertNotNull(mine, "应能查到刚提交的实测记录");
        assertEquals(120, mine.path("pm10").asInt());
        assertEquals(45, mine.path("no2").asInt());
        assertEquals(88, mine.path("o3").asInt());
        assertEquals(1, mine.path("gridMemberId").asInt());

        // 按网格员筛选
        mockMvc.perform(get("/api/air-quality-stats")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("gridMemberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThan(0)));

        // 时间范围之外的记录查不到
        mockMvc.perform(get("/api/air-quality-stats")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("startTime", "2000-01-01 00:00:00")
                        .param("endTime", "2000-01-02 00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(0)));

        // 开始时间晚于结束时间应被拒绝
        mockMvc.perform(get("/api/air-quality-stats")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("startTime", "2030-01-02 00:00:00")
                        .param("endTime", "2030-01-01 00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("开始时间不能晚于结束时间")));

        // 网格员与公众监督员也可查询；未登录被拒
        mockMvc.perform(get("/api/air-quality-stats").header(HttpHeaders.AUTHORIZATION, gridToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/air-quality-stats").header(HttpHeaders.AUTHORIZATION, supervisorToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/air-quality-stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aqiLevelDictionaryIsAvailableToFrontend() throws Exception {
        mockMvc.perform(get("/api/aqi-levels").header(HttpHeaders.AUTHORIZATION, bearer(loginAsAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(6)))
                .andExpect(jsonPath("$.data[0].levelId", is(1)))
                .andExpect(jsonPath("$.data[0].levelName", is("优")))
                .andExpect(jsonPath("$.data[0].minAqi", is(0)))
                .andExpect(jsonPath("$.data[0].maxAqi", is(50)))
                .andExpect(jsonPath("$.data[0].healthEffect", notNullValue()))
                .andExpect(jsonPath("$.data[5].levelName", is("严重污染")))
                .andExpect(jsonPath("$.data[5].maxAqi", is(500)));

        // 公众监督员同样需要级别字典
        mockMvc.perform(get("/api/aqi-levels")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAsSupervisor("supervisor001", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(6)));

        mockMvc.perform(get("/api/aqi-levels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void feedbackPageSupportsCityAndStatusFilters() throws Exception {
        String adminToken = bearer(loginAsAdmin());

        // 不带筛选条件
        String allBody = mockMvc.perform(get("/api/feedback")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("page", "1")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long total = objectMapper.readTree(allBody).path("data").path("total").asLong();
        long city1Total = objectMapper.readTree(
                        mockMvc.perform(get("/api/feedback")
                                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                                        .param("cityId", "1")
                                        .param("size", "50"))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString())
                .path("data").path("total").asLong();

        assertTrue(total > 0, "种子数据应有反馈记录");
        assertTrue(city1Total > 0 && city1Total < total, "按城市筛选后条数应减少");

        // 按状态筛选：结果里不应出现其他状态
        String statusBody = mockMvc.perform(get("/api/feedback")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("status", "2")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        for (JsonNode row : objectMapper.readTree(statusBody).path("data").path("records")) {
            assertEquals(2, row.path("status").asInt(), "按状态 2 筛选时不应返回其他状态");
        }

        // 城市 + 状态组合筛选
        mockMvc.perform(get("/api/feedback")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("cityId", "1")
                        .param("status", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray());

        // size 超出上限时被截断，避免一次拉取全表
        mockMvc.perform(get("/api/feedback")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()", lessThanOrEqualTo(100)));
    }

    @Test
    void dashboardStatisticsIncludesPollutantCharts() throws Exception {
        mockMvc.perform(get("/api/statistics/dashboard").header(HttpHeaders.AUTHORIZATION, bearer(loginAsAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.summary.totalFeedback").isNumber())
                .andExpect(jsonPath("$.data.provincePollutantStats").isArray())
                .andExpect(jsonPath("$.data.provincePollutantStats[0].provinceShortName", notNullValue()))
                .andExpect(jsonPath("$.data.provincePollutantStats[0].pm25ExceedCount").isNumber())
                .andExpect(jsonPath("$.data.provincePollutantStats[0].so2ExceedCount").isNumber())
                .andExpect(jsonPath("$.data.provincePollutantStats[0].coExceedCount").isNumber())
                .andExpect(jsonPath("$.data.aqiDistribution").isArray())
                .andExpect(jsonPath("$.data.monthlyTrend").isArray())
                .andExpect(jsonPath("$.data.refreshedAt", notNullValue()));
    }

    @Test
    void assignmentUsesLocalFirstAndCrossProvinceFallback() throws Exception {
        String adminToken = bearer(loginAsAdmin());
        String gridMemberToken = bearer(loginAsGridMember("SY-001"));

        mockMvc.perform(get("/api/feedback/1/assignment-candidates/local").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.hasLocalMembers", is(true)))
                .andExpect(jsonPath("$.data.crossProvinceEnabled", is(false)))
                .andExpect(jsonPath("$.data.members[0].cityId", is(1)));

        mockMvc.perform(get("/api/feedback/1/assignment-candidates/cross-province").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasLocalMembers", is(true)))
                .andExpect(jsonPath("$.data.crossProvinceEnabled", is(false)))
                .andExpect(jsonPath("$.data.members.length()", is(0)));

        mockMvc.perform(put("/api/feedback/1/assign")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gridMemberId": 5,
                                  "crossProvince": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("本市存在在岗网格员，不能发起外省调派")));

        mockMvc.perform(put("/api/feedback/1/assign")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gridMemberId": 1,
                                  "crossProvince": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedGridMemberId", is(1)))
                .andExpect(jsonPath("$.data.assignmentType", is("LOCAL")))
                .andExpect(jsonPath("$.data.status", is(1)));

        // 任务列表不再接收 gridMemberId 参数，身份完全来自令牌
        mockMvc.perform(get("/api/grid/tasks")
                        .header(HttpHeaders.AUTHORIZATION, gridMemberToken)
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].assignedGridMemberId", is(1)))
                .andExpect(jsonPath("$.data[0].status", is(1)));

        mockMvc.perform(post("/api/grid/tasks/1/measurements")
                        .header(HttpHeaders.AUTHORIZATION, gridMemberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "so2Value": 60,
                                  "coValue": 3.5,
                                  "pm25Value": 82
                                }
                                """))
                .andExpect(status().isOk())
                // 只提交浓度值，等级与 AQI 由服务端计算
                // SO2=60→63、CO=3.5→63、PM2.5=82→109，AQI 取最大 109（轻度污染）
                .andExpect(jsonPath("$.data.so2Level", is("良")))
                .andExpect(jsonPath("$.data.coLevel", is("良")))
                .andExpect(jsonPath("$.data.pm25Level", is("轻度污染")))
                .andExpect(jsonPath("$.data.aqiValue", is(109)))
                .andExpect(jsonPath("$.data.aqiLevel", is("轻度污染")))
                .andExpect(jsonPath("$.data.measuredAt", notNullValue()))
                // 实测完成不等于办结，仍停在"处理中"等待管理员处理
                .andExpect(jsonPath("$.data.status", is(1)))
                .andExpect(jsonPath("$.data.statusText", is("处理中")));

        mockMvc.perform(get("/api/feedback/3/assignment-candidates/local").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasLocalMembers", is(false)))
                .andExpect(jsonPath("$.data.crossProvinceEnabled", is(true)))
                .andExpect(jsonPath("$.data.members.length()", is(0)));

        mockMvc.perform(get("/api/feedback/3/assignment-candidates/cross-province").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasLocalMembers", is(false)))
                .andExpect(jsonPath("$.data.crossProvinceEnabled", is(true)))
                .andExpect(jsonPath("$.data.members[0].provinceId", notNullValue()));

        mockMvc.perform(put("/api/feedback/3/assign")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gridMemberId": 5,
                                  "crossProvince": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedGridMemberId", is(5)))
                .andExpect(jsonPath("$.data.assignmentType", is("CROSS_PROVINCE")))
                .andExpect(jsonPath("$.data.status", is(1)));
    }

    @Test
    void protectedEndpointsRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/feedback"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(401)));

        mockMvc.perform(get("/api/statistics/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(401)));

        mockMvc.perform(get("/api/grid/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(401)));

        mockMvc.perform(get("/api/grid/tasks").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(401)));

        // 缺少 Bearer 前缀同样视为未登录
        mockMvc.perform(get("/api/feedback").header(HttpHeaders.AUTHORIZATION, loginAsAdmin()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(401)));
    }

    @Test
    void gridMemberCannotAccessAdminEndpoints() throws Exception {
        String gridMemberToken = bearer(loginAsGridMember("SY-001"));

        mockMvc.perform(get("/api/feedback").header(HttpHeaders.AUTHORIZATION, gridMemberToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));

        mockMvc.perform(get("/api/statistics/dashboard").header(HttpHeaders.AUTHORIZATION, gridMemberToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));
    }

    @Test
    void adminCannotReadGridTasksAndCannotForgeOtherGridMember() throws Exception {
        String adminToken = bearer(loginAsAdmin());

        mockMvc.perform(get("/api/grid/tasks").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)));

        // 用「沈阳网格员」的令牌访问大连的反馈（种子数据 feedback 2，与指派用例无关），应被判定为无权处理
        String shenyangToken = bearer(loginAsGridMember("SY-001"));

        mockMvc.perform(get("/api/grid/tasks/2").header(HttpHeaders.AUTHORIZATION, shenyangToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("当前任务未指派给该网格员")));
    }
}
