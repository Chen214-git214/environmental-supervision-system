package com.neusoft.supervision.auth.security;

/**
 * 系统角色标识。存入 JWT 的 role 声明，并在拦截器中用于接口授权。
 */
public final class AuthRole {
    public static final String ADMIN = "ADMIN";
    public static final String GRID_MEMBER = "GRID_MEMBER";
    public static final String PUBLIC_SUPERVISOR = "PUBLIC_SUPERVISOR";

    private AuthRole() {
    }
}
