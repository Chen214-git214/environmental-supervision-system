package com.neusoft.supervision.auth.security;

/**
 * 身份合法但无权访问该接口（越权访问）时抛出，由全局异常处理器转换为 403。
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
