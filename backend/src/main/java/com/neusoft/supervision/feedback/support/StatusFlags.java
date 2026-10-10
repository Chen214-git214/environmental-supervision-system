package com.neusoft.supervision.feedback.support;

/**
 * 通用启用状态取值。数据库设计文档定义：1 启用、0 禁用。
 */
public final class StatusFlags {
    public static final int ENABLED = 1;
    public static final int DISABLED = 0;

    private StatusFlags() {
    }
}
