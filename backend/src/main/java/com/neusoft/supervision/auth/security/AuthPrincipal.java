package com.neusoft.supervision.auth.security;

/**
 * 从 JWT 中解析出的登录身份。
 *
 * @param userId   管理员编号或网格员编号
 * @param account  登录账号（管理员账号 / 网格员工号）
 * @param role     角色标识，见 {@link AuthRole}
 */
public record AuthPrincipal(Long userId, String account, String role) {
}
