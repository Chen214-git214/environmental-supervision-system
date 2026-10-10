package com.neusoft.supervision.auth.security;

/**
 * 认证失败（未携带 token、token 无效或已过期）时抛出，由全局异常处理器转换为 401。
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
