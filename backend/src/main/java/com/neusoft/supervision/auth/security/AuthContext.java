package com.neusoft.supervision.auth.security;

/**
 * 保存当前请求的登录身份，供业务层读取，避免接口再通过请求参数接收身份标识。
 *
 * <p>由 {@link AuthInterceptor} 在 preHandle 中写入、在 afterCompletion 中清理，防止线程复用导致的身份串号。</p>
 */
public final class AuthContext {
    private static final ThreadLocal<AuthPrincipal> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthPrincipal principal) {
        CURRENT.set(principal);
    }

    public static AuthPrincipal get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    /**
     * 获取当前登录的网格员编号。接口已由拦截器保证角色为网格员，因此这里只做防御性判断。
     */
    public static Long requireGridMemberId() {
        AuthPrincipal principal = CURRENT.get();

        if (principal == null || principal.userId() == null) {
            throw new UnauthorizedException("登录状态已失效，请重新登录");
        }

        return principal.userId();
    }

    /**
     * 获取当前登录的管理员编号。接口已由拦截器保证角色为管理员，因此这里只做防御性判断。
     */
    public static Long requireAdminId() {
        AuthPrincipal principal = CURRENT.get();

        if (principal == null || principal.userId() == null) {
            throw new UnauthorizedException("登录状态已失效，请重新登录");
        }

        return principal.userId();
    }

    /**
     * 获取当前登录的公众监督员编号。
     */
    public static Long requirePublicSupervisorId() {
        AuthPrincipal principal = CURRENT.get();

        if (principal == null || principal.userId() == null) {
            throw new UnauthorizedException("登录状态已失效，请重新登录");
        }

        return principal.userId();
    }
}
