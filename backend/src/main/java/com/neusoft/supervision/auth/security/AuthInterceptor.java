package com.neusoft.supervision.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 认证与授权拦截器。
 *
 * <p>职责：
 * <ol>
 *   <li>放行登录接口与 CORS 预检请求；</li>
 *   <li>校验 {@code Authorization: Bearer <token>}，无效或缺失时抛 401；</li>
 *   <li>按路径前缀校验角色，越权访问时抛 403；</li>
 *   <li>通过后把身份写入 {@link AuthContext}，并在请求结束时清理。</li>
 * </ol>
 * 业务接口因此不再需要（也不应）从请求参数里接收身份标识。</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOGIN_PREFIX = "/api/auth";
    private static final String ADMIN_PREFIX = "/api/feedback";
    private static final String STATISTICS_PREFIX = "/api/statistics";
    private static final String GRID_PREFIX = "/api/grid";
    /** 公众端仅注册接口公开，其余（如 /me）需要公众监督员身份 */
    private static final String PUBLIC_SUPERVISOR_REGISTER = "/api/public-supervisors";
    private static final String PUBLIC_SUPERVISOR_PREFIX = "/api/public-supervisors/";
    /** 公众监督员提交反馈与其自身反馈列表 */
    private static final String PUBLIC_FEEDBACK_PREFIX = "/api/public-feedback";
    /** 区域字典：三种角色都需要（公众端选城市、管理端筛选、网格端展示） */
    private static final String GRID_CITIES_PATH = "/api/grid-cities";
    /** AQI 级别字典：前端级别下拉使用 */
    private static final String AQI_LEVELS_PATH = "/api/aqi-levels";
    /** 实测数据查询：管理端统计、网格端回看、公众端查看自身相关数据 */
    private static final String AIR_QUALITY_STATS_PATH = "/api/air-quality-stats";

    private final JwtTokenService jwtTokenService;

    public AuthInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求不携带自定义请求头，必须放行，否则浏览器端的跨域调用会直接失败
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        if (isWhitelisted(path)) {
            return true;
        }

        AuthPrincipal principal = jwtTokenService.parse(resolveToken(request));

        if (!isAllowed(principal.role(), path)) {
            throw new ForbiddenException("当前角色无权访问该接口");
        }

        AuthContext.set(principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        // 线程会被复用，必须清理，否则下一个请求可能读到上一个请求的身份
        AuthContext.clear();
    }

    private boolean isWhitelisted(String path) {
        return path.startsWith(LOGIN_PREFIX)
                || PUBLIC_SUPERVISOR_REGISTER.equals(path);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || header.isBlank()) {
            throw new UnauthorizedException("请先登录");
        }

        if (!header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new UnauthorizedException("请先登录");
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            throw new UnauthorizedException("请先登录");
        }

        return token;
    }

    private boolean isAllowed(String role, String path) {
        List<String> allowedRoles = allowedRoles(path);
        return allowedRoles != null && allowedRoles.contains(role);
    }

    private List<String> allowedRoles(String path) {
        if (path.startsWith(GRID_CITIES_PATH) || path.startsWith(AQI_LEVELS_PATH)) {
            return List.of(AuthRole.ADMIN, AuthRole.GRID_MEMBER, AuthRole.PUBLIC_SUPERVISOR);
        }

        if (path.startsWith(ADMIN_PREFIX) || path.startsWith(STATISTICS_PREFIX)) {
            return List.of(AuthRole.ADMIN);
        }

        if (path.startsWith(AIR_QUALITY_STATS_PATH)) {
            return List.of(AuthRole.ADMIN, AuthRole.GRID_MEMBER, AuthRole.PUBLIC_SUPERVISOR);
        }

        if (path.startsWith(GRID_PREFIX)) {
            return List.of(AuthRole.GRID_MEMBER);
        }

        if (path.startsWith(PUBLIC_FEEDBACK_PREFIX) || path.startsWith(PUBLIC_SUPERVISOR_PREFIX)) {
            return List.of(AuthRole.PUBLIC_SUPERVISOR);
        }

        // 未显式登记的接口默认拒绝，避免新增接口时忘记配置权限
        return null;
    }
}
