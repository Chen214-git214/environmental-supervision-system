package com.neusoft.supervision.feedback.support;

/**
 * 反馈状态文案。0/1/2/3 由数据库设计文档定义，此前散落在服务类内部，抽出以便管理端、网格端、公众端共用同一套文案。
 */
public final class StatusText {
    public static final int PENDING = 0;
    public static final int PROCESSING = 1;
    public static final int DONE = 2;
    public static final int REJECTED = 3;

    private StatusText() {
    }

    public static String of(Integer status) {
        if (status == null) {
            return "-";
        }

        return switch (status) {
            case PENDING -> "待处理";
            case PROCESSING -> "处理中";
            case DONE -> "已完成";
            case REJECTED -> "已驳回";
            default -> "未知";
        };
    }
}
