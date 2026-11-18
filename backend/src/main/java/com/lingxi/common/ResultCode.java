package com.lingxi.common;

/**
 * 业务状态码。
 *
 * <p>约定：HTTP 状态统一返回 200，业务结果由 code 表达，前端按 code 分支处理。</p>
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无操作权限"),
    FAIL(500, "操作失败"),
    INTERNAL_ERROR(5000, "系统内部错误"),

    /* 请求解析：4001-4099 */
    REQUEST_BODY_ERROR(4001, "请求体格式错误"),
    PARAM_TYPE_MISMATCH(4002, "请求参数类型不匹配"),

    /* 用户与认证：2001-2099 */
    INVALID_CREDENTIALS(2001, "用户名或密码错误"),
    ACCOUNT_DISABLED(2002, "账号已被禁用"),
    USER_NOT_FOUND(2003, "用户不存在"),
    LOGIN_LOCKED(2004, "登录失败次数过多，请 5 分钟后再试"),

    /* 无人值守 Agent：7001-7099 */
    TASK_NOT_FOUND(7001, "定时任务不存在"),
    TASK_NOT_OWNED(7002, "无权操作该任务"),
    TASK_STATUS_INVALID(7003, "任务状态仅支持 ENABLED / DISABLED"),
    TASK_SCHEDULE_INVALID(7004, "任务调度配置不合法"),
    TASK_CONCURRENT_MODIFIED(7005, "任务已被其他操作修改，请刷新后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
